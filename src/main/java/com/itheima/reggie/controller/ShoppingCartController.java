package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.ShoppingCartService;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 购物车
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){//前端传来一个json数据
        log.info("加入购物车的信息是：{}", shoppingCart.toString());

        //获得当前用户id,userId
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //判断该用户是否加入了这道菜或套餐（dishId和 dishFlavor分别一致，表面该菜品存在）
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId);
        lambdaQueryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId, shoppingCart.getDishId());
        //lambdaQueryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        lambdaQueryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
        if(cartServiceOne!=null){
            cartServiceOne.setNumber(cartServiceOne.getNumber()+1);
            shoppingCartService.updateById(cartServiceOne);
        }
        else{
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        //获得当前用户id,userId
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId);
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);//按照加入菜品或套餐的时间升序排列
        List<ShoppingCart> list = shoppingCartService.list(lambdaQueryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("clean")
    public R<String> clean(){
        log.info("清空购物车...");

        //获得当前用户id,userId
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(lambdaQueryWrapper);

        return R.success("清空成功");
    }

    /**
     * 减少菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){//前端传来一个json数据
        log.info("待减少的菜品：{}", shoppingCart.toString());

        //获得当前用户id,userId
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //减少菜品或套餐
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId);
        lambdaQueryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId, shoppingCart.getDishId());
        lambdaQueryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
        if(cartServiceOne.getNumber()>1){
            cartServiceOne.setNumber(cartServiceOne.getNumber()-1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            shoppingCartService.remove(lambdaQueryWrapper);
        }

        return R.success("菜品减少成功");
    }

}
