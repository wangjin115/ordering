package com.wj.reggie.dto;

import com.wj.reggie.entity.Setmeal;
import com.wj.reggie.entity.SetmealDish;
import com.wj.reggie.entity.Setmeal;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
