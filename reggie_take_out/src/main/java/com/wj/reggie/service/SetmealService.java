package com.wj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.reggie.dto.SetmealDto;
import com.wj.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author wj
 * @version 1.0
 */
public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);

    public SetmealDto getByIdWithDish(Long id);

   public void updateWithDish(SetmealDto setmealDto);

    public void startAndEnd(Integer status, Long[] ids);

}
