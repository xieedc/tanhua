package com.tanhua.server.controller;

import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 消费者-提供请求和响应
 * 用户控制层
 */
@RestController
@RequestMapping("/user")
public class LoginController {
    @Autowired
    private UserService userService;

    /**
     * 根据手机号码 查询 用户对象
     * ResponseEntity(主要包含 状态码 返回内容)
     */
    @RequestMapping(value = "/findUser",method = RequestMethod.GET)
    public ResponseEntity findUser(String mobile){
        return userService.findByMobile(mobile);
    }

    /**
     * 保存用户
     * @param params
     * @return
     */
    @RequestMapping(value = "/saveUser",method = RequestMethod.POST)
    public ResponseEntity saveUser(@RequestBody Map<String,Object> params){
        String mobile = (String) params.get("mobile");
        String password = (String) params.get("password");
        return userService.saveUser(mobile,password);
    }

    /**
     * 注册登录第一步:发送验证码
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody Map<String,String>param){
        String phone = param.get("phone");
        userService.sendValidateCode(phone);
        return ResponseEntity.ok(null);
    }

    /**
     * 注册登录-第一步：验证码校验(登录)
     */
    @RequestMapping(value = "/loginVerification",method = RequestMethod.POST)
    public ResponseEntity loginVerification(@RequestBody Map<String,String> param){
        String phone = param.get("phone");
        String verificationCode = param.get("verificationCode");
        Map<String, Object> map = userService.loginVerification(phone,verificationCode);
        return ResponseEntity.ok(map);
    }
}
