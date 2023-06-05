package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request 登录成功后，将Employee对象的id存入session
     * @param employee post方法发送请求，其参数在body里
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1.将输入的密码使用md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.通过name在数据库中查询emp
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3.数据库中是否存在emp
        if(emp==null){
            return R.error("登录失败");
        }

        //4.密码是否正确
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //5.员工状态是否处于禁用
        if(0==emp.getStatus()){
            return R.error("账号已禁用");
        }

        //6.登录成功，将Employee对象的id存入session
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清除session中的数据
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping()
    public R<String> add(HttpServletRequest request, @RequestBody Employee employee){
        //1.初始时给默认密码，用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //2.补全employee的创建时间、修改时间、创建者、修改者 的操作利用mybatis plus的公共字段自动填充来做
        /*//2.补全employee的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //2.补全employee的创建者和修改者
        long uerId = (long) request.getSession().getAttribute("employee");//session中去除的数据都是object,需要强转
        employee.setCreateUser(uerId);
        employee.setUpdateUser(uerId);*/

        long uerId = (long) request.getSession().getAttribute("employee");//session中去除的数据都是object,需要强转
        BaseContext.setCurrentId(uerId);

        boolean save = employeeService.save(employee);
        return R.success("添加成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //log.info("page={},pageSize={},name={}",page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();

        //name条件(模糊查询)
        lambdaQueryWrapper.like(name != null, Employee::getName, name);
        //排序
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, lambdaQueryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }


    /**
     * 根据员工 id 查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id){
        log.info("根据id查询员工信息...");
        Employee emp = employeeService.getById(id);
        if(emp!=null){
            return R.success(emp);
        }
        return R.error("没有查询到员工信息");
    }
}
