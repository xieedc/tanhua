package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX="MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ","");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);
        Admin admin = null;
        if(StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey,30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 登录功能
     * @param map
     * @return
     */
    public Map login(Map<String, String> map) {
        //获取请求参数
        String username = map.get("username");
        String password = map.get("password");
        String verificationCode = map.get("verificationCode");
        String uuid = map.get("uuid");
        //2.非空判断
        if(StringUtils.isAnyEmpty(username,password,verificationCode,uuid)){
            throw new BusinessException("非法请求");
        }
        //验证码校验
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        String redisCode = (String) redisTemplate.opsForValue().get(key);
        if(!redisCode.equals(verificationCode)){
            throw new BusinessException("验证码错误");
        }
        //校验账号和密码
        Admin admin = query().eq("username", username).one();
        if (admin == null){
            throw new BusinessException("账号不存在");
        }
        if (!admin.getPassword().equals(SecureUtil.md5(password))){
            throw new BusinessException("密码错误");
        }

        //5.生成token
        String token = jwtUtils.createJWT(username, admin.getId());
        //6.将token存入redis
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminStr = JSON.toJSONString(admin);
        redisTemplate.opsForValue().set(tokenKey,adminStr);
        Map rsMap = new HashMap();
        rsMap.put("token",token);
        return rsMap;
    }


    /**
     * 用户退出
     * @param token
     */
    public void logout(String token) {
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        redisTemplate.delete(tokenKey);
    }
}
