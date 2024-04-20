package com.wj.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.entity.OrderDetail;
import com.wj.reggie.mapper.OrderDetailMapper;
import com.wj.reggie.service.OrderDetailService;
import com.wj.reggie.entity.OrderDetail;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}