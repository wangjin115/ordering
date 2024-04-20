package com.wj.reggie.service.impl;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.common.CustomException;
import com.wj.reggie.dto.DishDto;
import com.wj.reggie.entity.Dish;
import com.wj.reggie.entity.DishFlavor;
import com.wj.reggie.entity.Setmeal;
import com.wj.reggie.entity.SetmealDish;
import com.wj.reggie.mapper.DishMapper;
import com.wj.reggie.service.DishFlavorService;
import com.wj.reggie.service.DishService;
import com.wj.reggie.service.SetmealDishService;
import com.wj.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wj
 * @version 1.0
 */

/**
 * 料理を追加し、同時に対応する味データを保存する
 * IDに基づいて料理情報と対応する味情報を取得する
 * 味と共に更新する
 * 味テーブルを更新：現在の料理に対応する味をクリアしてから、現在の味データを追加する
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;

//    新增菜品，同时保存对应的口味数据
// 料理を追加し、同時に対応する味データを保存する
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        // 菜品の基本情報を料理テーブルに保存する
        this.save(dishDto);

        Long dishId = dishDto.getId();//料理id

        //菜品口味　料理の味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors =flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());


        //保存菜品口味数据到菜品口味表dish_flavor
        //料理の味データを料理の味テーブルに保存する
       dishFlavorService.saveBatch(flavors);


    }

    //根据id查询菜品信息和对应口味信息
    // IDに基づいて料理情報と対応する味情報を取得する
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        // 料理基本情報を取得、dishテーブルから
        Dish dish = this.getById(id);
        //在类的一个方法中调用同一个类中的另一个方法时，可以使用 this 来显式地表示当前对象。

        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品对应的口味信息，从dish_flavor表查询
        // 料理の味情報を取得、dish_flavorテーブルから
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors=dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(flavors);

        return dishDto;
    }
    // 更新数据库  点提交的时候就更新数据库
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表
        // 料理テーブルを更新する
        this.updateById(dishDto);

        //更新口味表：先清理当前菜品对应的口味delete，再添加当前提交过来的口味数据insert
        // 味テーブルを更新：現在の菜品に対応する味をクリアしてから、現在の味データを追加する
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors =flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
        //如果不进行这个操作，只是在数据库里保存了name跟flavor，并不知道是哪个菜品的，所以通过遍历给每个flavor赋上id


    }

    @Override
    public void removeWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper =new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        int count = this.count(dishLambdaQueryWrapper);
        if (count>0){
            throw new CustomException("この料理は販売中のため削除できません。");
        }
        this.removeByIds(ids);

        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);

        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);

    }

    @Override
    @Transactional
    public void startAndEnd(Integer status, Long[] ids) {

        ArrayList<Long> idList = new ArrayList<>();
        for (Long id:ids){
            idList.add(id);
        }

        LambdaUpdateWrapper<Dish> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Dish::getStatus,status).in(Dish::getId,ids);

        this.update(lambdaUpdateWrapper);
//        for (int i = 0; i < ids.length; i++) {
//            Long id=ids[i];
//            Dish dish=this.getById(id);
//            dish.setStatus(status);
//            this.updateById(dish);
//
//        }

        if (status==0){
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(SetmealDish::getDishId,ids);
            //根据菜品id查询套餐id

            List<SetmealDish> setmealDishes =setmealDishService.list(lambdaQueryWrapper);
            List<Long> setmealId=new ArrayList<>();
            for (SetmealDish setmealDish:setmealDishes){
                setmealId.add(setmealDish.getSetmealId());
            }

            LambdaUpdateWrapper<Setmeal> lambdaUpdateWrapper1=new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper1.set(Setmeal::getStatus,status).in(Setmeal::getId,setmealId);

            setmealService.update(lambdaUpdateWrapper1);


        }
    }
}
