package com.wj.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.reggie.entity.AddressBook;
import com.wj.reggie.mapper.AddressBookMapper;
import com.wj.reggie.service.AddressBookService;
import com.wj.reggie.entity.AddressBook;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
