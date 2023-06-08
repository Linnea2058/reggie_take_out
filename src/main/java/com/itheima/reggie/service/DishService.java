package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品和对应的口味信息
    public void saveWithFlavor(DishDto dishDto);


    //通过菜品id，查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(long id);

    //修改菜品和对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
