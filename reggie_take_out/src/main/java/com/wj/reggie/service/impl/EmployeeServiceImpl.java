package com.wj.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.entity.Employee;
import com.wj.reggie.mapper.EmployeeMapper;
import com.wj.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author wj
 * @version 1.0
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
