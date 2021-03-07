package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class SettingsController {
    @Autowired
    private SettingsService settingsService;

    /**
     * 查询通用设置
     */
    @RequestMapping(value = "/settings",method = RequestMethod.GET)
    public ResponseEntity settings(){
        SettingsVo vo = settingsService.settings();
        return ResponseEntity.ok(vo);
    }

    /**
     * 通知设置  - 保存
     */
    @RequestMapping(value = "/notifications/setting",method = RequestMethod.POST)
    public ResponseEntity notifications(@RequestBody Map map){
        boolean likeNotification = (boolean) map.get("likeNotification");
        boolean pinglunNotification = (boolean) map.get("pinglunNotification");
        boolean gonggaoNotification =  (boolean)map.get("gonggaoNotification");
        settingsService.notifications(likeNotification,pinglunNotification,gonggaoNotification);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询黑名单列表
     */
    @RequestMapping(value = "/blacklist",method = RequestMethod.GET)
    public ResponseEntity findBlackList(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pagesize){
        PageResult pageResult = settingsService.findBlackList(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 移除黑名单
     * GET POST DELTE PUT 等
     * http://localhost:10880/user/blacklist/1 delete
     */
    @RequestMapping(value = "/blacklist/{uid}",method = RequestMethod.DELETE)
    public ResponseEntity delBlacklist(@PathVariable("uid") String deleteUserId){
        settingsService.delBlacklist(deleteUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 设置陌生人问题 - 保存
     */
    @RequestMapping(value = "/questions",method = RequestMethod.POST)
    public ResponseEntity questions(@RequestBody Map map){
        settingsService.questions((String) map.get("content"));
        return ResponseEntity.ok(null);
    }



}
