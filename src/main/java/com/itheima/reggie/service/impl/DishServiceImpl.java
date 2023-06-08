package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品和对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);//将dishDto中dish的信息添加到dish表

        //可以从dishDto中获得口味数据（name、value），并且需要补充dish_id
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{//匿名内部类，操作flavors中的数据
            item.setDishId(dishDto.getId());//获得并设置dish_id
            return item;
        }).collect(Collectors.toList());//collectors转成list

        dishFlavorService.saveBatch(flavors);//批量插入
    }

    /**
     * 获取菜品和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(long id) {

        //根据id查dish
        Dish dish = dishService.getById(id);

        //根据dish_id查flavor
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors= dishFlavorService.list(lambdaQueryWrapper);

        //复制
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新菜品和对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {

        //将dishDto中dish的信息修改到dish表
        this.updateById(dishDto);

        //删除已有的口味信息
        LambdaQueryWrapper<DishFlavor> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw1);

        //添加口味信息
        //从dishDto中获得口味数据（name、value），并且需要补充dish_id
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{//匿名内部类，操作flavors中的数据
            item.setDishId(dishDto.getId());//获得并设置dish_id
            return item;
        }).collect(Collectors.toList());//collectors转成list
        dishFlavorService.saveBatch(flavors);//批量插入
    }


}
