package com.wj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.reggie.common.R;
import com.wj.reggie.dto.SetmealDto;
import com.wj.reggie.entity.Category;
import com.wj.reggie.entity.Dish;
import com.wj.reggie.entity.Setmeal;
import com.wj.reggie.service.CategoryService;
import com.wj.reggie.service.SetmealDishService;
import com.wj.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐　　新しいメニューセットを追加します
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("メニューセット情報：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新しいメニューセットを追加しました");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
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
        //分页构造器对象　　 // ページング構築オブジェクトの作成
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        // 名前に基づいて部分一致検索条件を追加します
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        // 更新日時で降順に並べ替える条件を追加します
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        // ページング情報をDTOにコピーします
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        // メニューセットのリストをDTOにマッピングします
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝　// オブジェクトのコピーを実行します
            BeanUtils.copyProperties(item,setmealDto);
            //分类id　　  // カテゴリIDを取得します
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象　　 // カテゴリIDに基づいてカテゴリオブジェクトを取得します
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称　　 // カテゴリ名を設定します
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        // DTOにリストを設定します
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐　　メニューセットを削除します
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("メニューセットを削除しました");
    }
//    IDに基づいてメニューセット情報を取得します
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){

        log.info("IDに基づいてメニューセット情報を取得します：{}",id);
        // IDに基づいてメニューセット情報を取得します
       SetmealDto setmealDto= setmealService.getByIdWithDish(id);

       return R.success(setmealDto);
    }
//    メニューセット情報を更新します
    @PutMapping
    public R<String> updateWithDish(@RequestBody SetmealDto setmealDto){
        log.info("メニューセット情報の更新：{}",setmealDto);
        setmealService.updateWithDish(setmealDto);

        return R.success("メニューセット情報を更新しました");
    }


    //更改套餐状态　 メニューセットのステータスを更新します

    @PostMapping("/status/{status}")
    public R<String> updateSetmealStatus(@PathVariable Integer status,Long[] ids){

        setmealService.startAndEnd(status,ids);

        return R.success("メニューセット情報を更新しました");
    }



    /**
     * 根据条件查询套餐数据 点击套餐时返回套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
}
