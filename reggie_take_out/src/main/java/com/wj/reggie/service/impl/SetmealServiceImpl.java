package com.wj.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.common.CustomException;
import com.wj.reggie.dto.SetmealDto;
import com.wj.reggie.entity.Dish;
import com.wj.reggie.entity.Setmeal;
import com.wj.reggie.entity.SetmealDish;
import com.wj.reggie.mapper.SetmealMapper;
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
//新增套餐，并在数据库中更新，保存到套餐信息表和菜品的关联关系表
    //新增套餐、并在データベースに更新し、套餐情報テーブルと料理の関連テーブルに保存します。
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;

//    新增套餐，保存数据到套餐表和套餐菜品表
// 新增套餐、データを套餐情報テーブルと套餐料理テーブルに保存します。
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        // 套餐の基本情報を保存します。setmealテーブルに挿入を行います。
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        // 套餐と料理の関連情報を保存します。setmeal_dishテーブルに挿入を行います。
        setmealDishService.saveBatch(setmealDishes);
    }

    //删除套餐，同时删除套餐和菜品关联的数据
    // 套餐を削除し、同時に套餐と料理の関連データを削除します。
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        // 套餐の状態を調べて、削除できるかどうかを確認します。
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            // 削除できない場合、業務例外をスローします。
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        // 削除できる場合、まず套餐テーブルからデータを削除します。--- setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        // 関連テーブルのデータを削除します。---- setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {

        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes=setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void startAndEnd(Integer status, Long[] ids) {

        ArrayList<Long> setmealIdList = new ArrayList<>();
        for (Long id:ids){
            setmealIdList.add(id);
        }

        LambdaUpdateWrapper<Setmeal> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Setmeal::getStatus,status).in(Setmeal::getId,ids);
  //更新选定菜品的状态
        // 選択した套餐のステータスを更新します。
        this.update(lambdaUpdateWrapper);
//        for (int i = 0; i < ids.length; i++) {
//            Long id=ids[i];
//            Dish dish=this.getById(id);
//            dish.setStatus(status);
//            this.updateById(dish);
//
//        }

        if (status==1){
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
             //查询setmealdish表
            // setmealdishテーブルをクエリします。

            //根据套餐id查询菜品id
            // 套餐IDに基づいて料理IDをクエリします。
            List<SetmealDish> setmealDishes =setmealDishService.list(lambdaQueryWrapper);
            List<Long> dishId=new ArrayList<>();
            for (SetmealDish setmealDish:setmealDishes){
                dishId.add(setmealDish.getDishId());
            }

            LambdaUpdateWrapper<Dish> lambdaUpdateWrapper1=new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper1.set(Dish::getStatus,status).in(Dish::getId,dishId);

            dishService.update(lambdaUpdateWrapper1);


        }
    }
}
