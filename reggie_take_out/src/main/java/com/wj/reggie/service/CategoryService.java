package com.wj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.reggie.entity.Category;

/**
 * @author wj
 * @version 1.0
 */
public interface CategoryService extends IService<Category> {
    public void remove(Long id);

}
