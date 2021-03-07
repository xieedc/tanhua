package com.tanhua.server.controller;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 完善个人信息
     * @RequestHeader:请求头参数
     * Authorization:参数不能随便写 前后端一定商量好
     */
    @RequestMapping(value = "/loginReginfo",method = RequestMethod.POST)
    public ResponseEntity loginReginfo(@RequestHeader("Authorization") String token, @RequestBody UserInfoVo userInfoVo){
        //1.将UserInfoVo转UserInfo
        UserInfo  userInfo = new UserInfo();
        //2.推荐使用 从userInfoVo copy到 userInfo 注意属性名称得一样才可以
        BeanUtils.copyProperties(userInfoVo,userInfo);
        userService.loginReginfo(userInfo,token);
        return ResponseEntity.ok(null);
    }

    /**
     * 上传头像
     */
    @RequestMapping(value = "/loginReginfo/head",method = RequestMethod.POST)
    public ResponseEntity loginReginfoHead(@RequestHeader("Authorization") String token, MultipartFile headPhoto){
        userService.loginReginfoHead(headPhoto,token);
        return ResponseEntity.ok(null);
    }
}
