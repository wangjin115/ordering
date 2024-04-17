package com.wj.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.entity.DishFlavor;
import com.wj.reggie.mapper.DishFlavorMapper;
import com.wj.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * @author wj
 * @version 1.0
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
