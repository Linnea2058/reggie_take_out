package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;



    /**
     * 保存套餐信息（带有菜品信息）
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //套餐信息保存到套餐表
        this.save(setmealDto);

        //setmealdish表中缺少setmealId信息，需要补齐后放入setmealdish表中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map(item->{
            item.setSetmealId(setmealDto.getId());//获得并设置setmealId信息
            return item;
        }).collect(Collectors.toList());//收集items转成list

        setmealDishService.saveBatch(setmealDishes);

    }

    @Override
    public void deleteWithDish(List<Long> ids) {
        //查看套餐是否处于售卖阶段
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId, ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);//正在售卖
        int count = setmealService.count(lambdaQueryWrapper);
        //处于售卖阶段，不删除
        if(count>0){
            new CustomException("套餐正在售卖无法删除");
        }
        //删除套餐
        setmealService.removeByIds(ids);

        //在setmealdish中删除套餐对应的dish
        LambdaQueryWrapper<SetmealDish> lwqDish = new LambdaQueryWrapper<>();
        lwqDish.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lwqDish);
    }
}