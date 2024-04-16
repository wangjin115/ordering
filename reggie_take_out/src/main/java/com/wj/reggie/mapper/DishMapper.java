package com.wj.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wj
 * @version 1.0
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
