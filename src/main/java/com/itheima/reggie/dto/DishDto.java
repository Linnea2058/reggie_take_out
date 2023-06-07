package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {//数据传输对象，继承自Dish，所以包含Dish的所有属性

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName; //分类名称

    private Integer copies;
}
