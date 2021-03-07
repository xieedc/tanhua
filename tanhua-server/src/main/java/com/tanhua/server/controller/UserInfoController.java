package com.tanhua.server.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserInfoService;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息管理控制层
 */
@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息
     *
     * @param userID
     * @param huanxinID
     * @param token
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getUserInfo(Long userID, Long huanxinID, @RequestHeader("Authorization") String token) {
        //1优先使用huanxinID  再使用userID
        Long userId = huanxinID;
        //2.如果huanxinID userID为空 从token获取userId
        if (userId == null) {
            userId = userID;
        }
        if (userId == null) {
            //3.调用servcie业务层处理
            UserInfoVo userInfoVo = userInfoService.getUserInfo(token);
            return ResponseEntity.ok(userInfoVo);
        }
        return null;
    }

    /**
     * 更新用户信息
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody UserInfoVo userInfoVo) {
        userInfoService.updateUserInfo(token,userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 更新用户头像
     */
    @RequestMapping(value = "/header",method = RequestMethod.POST)
    public ResponseEntity header(@RequestHeader("Authorization") String token, MultipartFile headPhoto){
        userInfoService.header(token,headPhoto);
        return ResponseEntity.ok(null);
    }

    /**
     * 互相喜欢、喜欢、粉丝统计
     */
    @RequestMapping(value = "/counts",method = RequestMethod.GET)
    public ResponseEntity counts() {
        CountsVo countsVo =  userService.counts();
        return ResponseEntity.ok(countsVo);
    }

    /**
     * 互相喜欢、喜欢、粉丝、谁看过我分页列表查询
     */
    @RequestMapping(value = "/friends/{type}",method = RequestMethod.GET)
    public ResponseEntity queryUserLikeList(@PathVariable int type,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pagesize) {
        PageResult<FriendVo> pageResult =  userService.queryUserLikeList(type,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 粉丝-喜欢
     */
    @RequestMapping(value = "/fans/{uid}",method = RequestMethod.POST)
    public ResponseEntity fansLike(@PathVariable("uid") Long fansUserId){
        userService.fansLike(fansUserId);
        return ResponseEntity.ok(null);
    }
}