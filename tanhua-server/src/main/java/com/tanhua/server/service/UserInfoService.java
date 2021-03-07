package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户信息管理业务处理层
 */
@Service
@Slf4j
public class UserInfoService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserInfoApi userInfoApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceTemplate faceTemplate;

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    public UserInfoVo getUserInfo(String token) {
        log.info("查询用户信息token{}：：：",token);
        UserInfoVo userInfoVo = new UserInfoVo();
        //a.根据TOKEN_xxxx获取用户对象
        /*String userStr = redisTemplate.opsForValue().get("TOKEN_" + token);
        //b.如果用户对象不存在 则重新登录
        if (StringUtils.isEmpty(userStr)) {
            throw new TanHuaException("登陆超时，请重新登陆");
        }

        // c.如果存在则调用服务提供者 根据用户id查询用户信息tb_user_info 返回UserInfo
        User user = JSON.parseObject(userStr, User.class);
        Long userId = user.getId();*/
        Long userId = UserHolder.getUserId();
        log.info("查询用户信息userId{}：：：",userId);
        UserInfo userInfo = userInfoApi.findByUserId(userId);
        //d.将UserInfo转为UserInfoVo (注意年龄字段单独处理 )
        BeanUtils.copyProperties(userInfo,userInfoVo);
        if(userInfo.getAge() != null){
            userInfoVo.setAge(String.valueOf(userInfo.getAge().intValue()));
        }
        log.info("查询用户信息userInfoVo{}：：：",userInfoVo);
        return userInfoVo;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(String token, UserInfoVo userInfoVo) {
        log.info("更新用户信息::::token{}::::userInfoVo::::{}",token,userInfoVo);
        //a.根据TOKEN_xxxx获取用户对象
        /*String userStr = redisTemplate.opsForValue().get("TOKEN_" + token);
        //b.如果用户对象不存在 则重新登录
        if (StringUtils.isEmpty(userStr)) {
            throw new TanHuaException("登陆超时，请重新登陆");
        }

        // c.如果存在则调用服务提供者 根据用户id查询用户信息tb_user_info 返回UserInfo
        User user = JSON.parseObject(userStr, User.class);
        Long userId = user.getId();*/
        Long userId = UserHolder.getUserId();
        log.info("查询用户信息userId{}：：：",userId);
        UserInfo userInfo = new UserInfo();
        //c.将UserInfoVo转为UserInfo
        BeanUtils.copyProperties(userInfoVo,userInfo);
        //d.从token中获取userId,如果存在则调用服务提供者 根据用户id修改用户信息tb_user_info
        userInfo.setId(userId);
        userInfo.setAge(GetAgeUtil.getAge(userInfo.getBirthday()));
        log.debug("查询用户信息userInfo{}：：：",userInfo);
        userInfoApi.update(userInfo);
    }

    /**
     * 更新用户头像
     */
    public void header(String token, MultipartFile headPhoto) {

        log.info("头像上传{},token{}",headPhoto.getOriginalFilename(),token);
        try {
            //a.根据TOKEN_xxxx获取用户对象
           /* String userStr = redisTemplate.opsForValue().get("TOKEN_" + token);
            //b.如果用户对象不存在 则重新登录
            if(StringUtils.isEmpty(userStr)){
                throw new TanHuaException("登陆超时，请重新登陆");
            }
            User user = JSON.parseObject(userStr, User.class);
            Long userId = user.getId();
*/
            Long userId = UserHolder.getUserId();
            //1.根据用户id查询用户信息userInfo
            UserInfo oldUserInfo = userInfoApi.findByUserId(userId);
            String oldAvatar = oldUserInfo.getAvatar();
            //c.如果存在则调用人脸识别 失败 返回“没有检测到人脸 重新上传”
            boolean detect = faceTemplate.detect(headPhoto.getBytes());
            if(!detect){
                throw new TanHuaException("没有检测到人脸，请重新上传");
            }
            log.info("人脸识别成功了....");
            //b.如果识别成功，调用上图图片组件 失败返回 上传失败
            String filename = headPhoto.getOriginalFilename();
            String avatar = ossTemplate.upload(filename, headPhoto.getInputStream());
            log.info("图片上传成功了....");
            //f.上传图片成功，调用服务提供者更新头像 （根据用户id更新tb_user_info 中头像字段）

            UserInfo userInfo  = new UserInfo();//更新头像
            userInfo.setAvatar(avatar);//头像
            userInfo.setId(userId);
            userInfoApi.update(userInfo);
            log.info("头像完善成功了....");

            //2.删除头像
            ossTemplate.deleteFile(oldAvatar);
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("上传头像失败",e);
            throw new TanHuaException("上传头像失败，请稍后重试");
        }

    }
}
