package com.tanhua.manage.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.interceptor.AdminHolder;
import com.tanhua.manage.service.AdminService;
import com.tanhua.manage.vo.AdminVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 后台登陆时 图片验证码 生成
     */
    @GetMapping("/verification")
    public void showValidateCodePic(String uuid,HttpServletRequest req, HttpServletResponse res){
        res.setDateHeader("Expires",0);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        res.setHeader("Pragma", "no-cache");
        res.setContentType("image/jpeg");
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(299, 97);
        String code = lineCaptcha.getCode();
        log.debug("uuid={},code={}",uuid,code);
        adminService.saveCode(uuid,code);
        try {
            lineCaptcha.write(res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录功能
     * @param map
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody Map<String,String> map){
        Map token = adminService.login(map);
        return ResponseEntity.ok(token);
    }

    /**
     * 用户基本信息
     * @return
     */
    @RequestMapping(value = "/profile",method = RequestMethod.POST)
    public ResponseEntity profile(){
        AdminVo adminVo = new AdminVo();
        //通过AdminHolder.getAdmin()获取Admin对象（ThreadLocal）
        Admin admin = AdminHolder.getAdmin();
        //将Admin对象copy到AdminVo
        BeanUtils.copyProperties(admin,adminVo);
        //返回VO
        return ResponseEntity.ok(adminVo);
    }

    /**
     * 用户退出
     * @return
     */
    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public ResponseEntity logout(@RequestHeader("Authorization") String authorization){
        String token = authorization.replace("Bearer","");
        adminService.logout(token);
        return ResponseEntity.ok(null);

    }


}