package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新建菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    private R<String> save(@RequestBody DishDto dishDto){
        //旁路缓存模式:1.更新数据库 2.删除缓存
        //更新数据库
        log.info("待添加的菜品信息：{}",dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        // 删除缓存
        //1.查看菜品的类型，构造 key
        String key = "dish_"+dishDto.getCategoryId()+'_'+dishDto.getStatus();
        //2.删除缓存中这类菜品的信息
        redisTemplate.delete(key);


        return R.success("添加菜品成功");
    }

    /**de
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

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable long id){

        log.info("菜品的id是:{}",id);


        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    private R<String> update(@RequestBody DishDto dishDto){

        log.info("待修改的菜品信息：{}",dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        // 删除缓存
        //1.查看菜品的类型，构造 key
        String key = "dish_"+dishDto.getCategoryId()+'_'+dishDto.getStatus();
        //2.删除缓存中这类菜品的信息
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    /**
     * 根据菜品分类的id查询菜品信息
     * @param dish
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish){//用Dish接收参数，通用性更强，dish中的信息都能接到
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){//改造一下，连同口味信息一起返回
        List<DishDto> dishDtoList =null;

        //以下面这种形式构造key
        String key = "dish_"+dish.getCategoryId()+'_'+dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        // 缓存中存在 菜品数据
        if(dishDtoList !=null){
            return R.success(dishDtoList);
        }

        // 缓存中不存在数据，查询数据库，并存入缓存
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        // 存入redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
