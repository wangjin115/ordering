package com.wj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.reggie.dto.DishDto;
import com.wj.reggie.entity.Dish;

/**
 * @author wj
 * @version 1.0
 */
public interface DishService extends IService<Dish> {
//新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish，dishflavor
// 菜品を追加し、同時に菜品に関連する味データを挿入する。dishテーブルとdishflavorテーブルの両方を操作する必要がある。
    public void saveWithFlavor(DishDto dishDto);
//根据id查询菜品信息和对应的口味信息
// IDに基づいて菜品情報と対応する味情報を取得する。
    public DishDto getByIdWithFlavor(Long id);

    // 味と共に菜品を更新する。

    public void updateWithFlavor(DishDto dishDto);

}
