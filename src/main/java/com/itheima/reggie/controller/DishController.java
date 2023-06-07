package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivilegedAction;


/**
 * 上传和下载文件
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

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
}
