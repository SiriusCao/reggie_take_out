package com.cao.reggie.dto;

import com.cao.reggie.entity.Setmeal;
import com.cao.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
