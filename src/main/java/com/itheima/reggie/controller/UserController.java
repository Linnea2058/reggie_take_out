package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;


/**
 * 客户登录
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送短信验证码
     * @param user
     * @param httpSession
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession) {//前端传来一个json数据，包含电话号码，可以用user接
        log.info("移动用户的电话号码是：{}", user.getPhone());

        // 获取手机号
        String phone = user.getPhone();

        if (phone!=null) {
            // 调用aliyun提供的api，获得6位的验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();

            // 调用aliyun提供的api发送验证码
            //SMSUtils.sendMessage(signName, templateCode,phone,code);
            log.info("短信验证码是：{}", code);

            // 验证码保存在session
            httpSession.setAttribute(phone, code);

            return R.success("验证码生成成功");
        }

        return R.error("验证吗发送失败");
    }

    @PostMapping("/login")
    public R<User> login(HttpSession session, @RequestBody Map map){
        log.info("用户输入的电话号码和code:{}",map.toString());

        //通过phone获取session中的验证码
        String phone = map.get("phone").toString();
        String codeInSesson = session.getAttribute(phone).toString();

        //比较输入的验证码和session中的验证码
        String code = map.get("code").toString();
        if(codeInSesson!=null && codeInSesson.equals(code)){
            //查看用户是否已经存在
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            //用户不存在，将其电话号码存入user表
            if(user==null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
