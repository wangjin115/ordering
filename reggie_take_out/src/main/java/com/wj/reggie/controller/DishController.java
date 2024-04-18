package com.wj.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.reggie.common.R;
import com.wj.reggie.dto.DishDto;
import com.wj.reggie.entity.*;
import com.wj.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wj
 * @version 1.0
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;


    //新增菜品　料理の追加
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("料理の追加に成功しました");
    }
   // ページ番号、ページサイズ、名前に基づいて料理をページングして取得する
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        try {
            // 解码 URL 编码的名称参数
            if (name != null) {
                name = URLDecoder.decode(name, StandardCharsets.UTF_8.toString());
            }
        } catch (UnsupportedEncodingException e) {
            // URL 解码失败，可以根据实际情况处理异常
            e.printStackTrace();
            // 返回错误信息或者采取其他措施
            return R.error("URL 解码失败");
        }

        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.like(name!=null, Dish::getName,name); //Dish::getName数据库信息

        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

      //  List<DishDto> list=null;
        List<DishDto> list=records.stream().map((item) -> {
            DishDto dishDto=new DishDto();

            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category!=null){
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
    // IDに基づいて料理を取得する
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    //修改菜品　　料理を更新する
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        return R.success("料理の更新に成功しました");
    }

    //根据传过来的条件查询对应菜品数据 添加菜品对话框中
    // 条件に基づいて料理データを取得する
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);  //菜品状态在售才显示

        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }

    //更改菜品状态
    //料理のステータスを変更する
    @PostMapping("/status/{status}")
    public R<String> updatestatusById(@PathVariable Integer status,Long[] ids){
        log.info("IDに基づいて料理の状態を変更します：{}, ID: {}",status,ids);

        dishService.startAndEnd(status,ids);
        return R.success("料理のステータスを変更しました");
    }

    //删除菜品
    //料理の削除
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        dishService.removeWithFlavor(ids);

        return R.success("料理を削除しました");
    }

//    @DeleteMapping
//    public R<String> delect(@RequestParam List<Long> ids){
//        log.info("ids:{}",ids);
//
//        dishService.deletWithSetmeal(ids);
//
//    }



}
