package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 上传和下载文件
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新建菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    private R<String> save(@RequestBody DishDto dishDto){

        log.info("待添加的菜品信息：{}",dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return R.success("添加菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //构造分页构造器
        Page<Dish> pageInfo = new Page<Dish>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();

        //name条件(模糊查询)
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        //排序
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo, lambdaQueryWrapper);

        //拷贝分页信息
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");//records 存放Dish对象信息，不拷贝，需要修改存放DishDto的对象信息

        //获得pageInfo中的records信息
        List<Dish> records = pageInfo.getRecords();

        //在dishDtoPage的records中 加入categoryName信息
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);//dish内容复制到dishDto，还差 categoryName

            //根据id查询分类对象
            Long categoryId = item.getCategoryId();//分类id
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);//dishDto中加入categoryName
            }
            return dishDto;
        }).collect(Collectors.toList());//所有的dishDto搜集起来，转成list

        //设置dishDtoPage的records信息
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
}
