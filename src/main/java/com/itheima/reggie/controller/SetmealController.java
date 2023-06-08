package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 套餐相关操作
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){//要接收套餐信息（setmeal表）和套餐中菜品信息（setmealDish表）
        log.info(setmealDto.toString());

        setmealService.saveWithDish(setmealDto);//套餐信息包含菜品信息
        return R.success("添加套餐成功");
    }


    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);


        //条件构造器
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();

        //name条件(模糊查询)
        lambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        //排序
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //执行查询
        setmealService.page(pageInfo, lambdaQueryWrapper);

        //拷贝分页信息
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        //获得pageInfo中的records信息
        List<Setmeal> records = pageInfo.getRecords();

        //在setmealDtoPage的records中 加入categoryName信息
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto); //setmeal内容复制到setmealDto，还差 categoryName

            //根据id查询分类对象
            Long categoryId = item.getCategoryId();//分类id
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);//setmealDtoPage中加入categoryName
            }
            return setmealDto;
        }).collect(Collectors.toList());//所有的setmealDtoPage搜集起来，转成list

        //设置setmealDtoPage的records信息
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

        log.info("待删除的套餐的id:{}",ids);

        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }
}
