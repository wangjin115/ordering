package com.wj.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.common.CustomException;
import com.wj.reggie.entity.Category;
import com.wj.reggie.entity.Dish;
import com.wj.reggie.entity.Setmeal;
import com.wj.reggie.mapper.CategoryMapper;
import com.wj.reggie.service.CategoryService;
import com.wj.reggie.service.DishService;
import com.wj.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wj
 * @version 1.0
 * カテゴリサービスの実装クラス
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * IDに基づいてカテゴリを削除する。削除前に判断を行う。
     * @param id
     */
    @Override
    public void remove(Long id) {
        //mybatis plus里面的一个查询构造器，我要先写上这个再进行查询　
        // データベースからのクエリ作成
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        // クエリ条件の追加：分類IDで検索
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);//eq(Dish::getCategoryId,id)相当于sql语句中where后面的语句
// データベースから菜品の数をカウント
        int count1 = dishService.count(dishLambdaQueryWrapper);//service中提供处理数据的方法

        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        // 分類が関連付けられているかをチェックし、関連付けられている場合はビジネス例外をスローします

        if(count1 >0){
        //count>0,说明当前分类关联了菜品,抛出一个业务异常
            throw new CustomException("この分類に関連付けられた料理がありますので、削除できません");

        }
        //查询当前分类是否关联了套餐，如果已经关联，跑出一个业务异常
        // データベースからのクエリ作成
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        // クエリ条件の追加：カテゴリIDで検索
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count();
        if(count2>0){
            //count>0,说明当前分类关联了套餐,抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");

        }

        //分類を削除
        super.removeById(id);


    }
}
