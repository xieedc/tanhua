package com.tanhua.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 消息管理控制层
 */
@RestController
@RequestMapping("messages")
public class IMController {
    @Autowired
    private IMService imService;

    /**
     * 联系人添加
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/contacts",method = RequestMethod.POST)
    public ResponseEntity addContacts(@RequestBody Map<String,Long> paramMap){
        imService.addContacts(paramMap);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/contacts",method = RequestMethod.GET)
    public ResponseEntity queryContacts(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pagesize,
                                        @RequestParam(required = false) String keyword){
        PageResult<ContactVo> pageResult = imService.queryContacts(page,pagesize,keyword);
        return ResponseEntity.ok(pageResult);

    }

    /**
     * 点赞列表
     *  //评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/likes",method = RequestMethod.GET)
    public ResponseEntity likes(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(page,pagesize,1);
        return ResponseEntity.ok(pageResult);
    }
    /**
     * 评论列表
     */
    @RequestMapping(value = "/comments",method = RequestMethod.GET)
    public ResponseEntity comments(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(page,pagesize,2);
        return ResponseEntity.ok(pageResult);
    }
    /**
     * 喜欢列表
     */
    @RequestMapping(value = "/loves",method = RequestMethod.GET)
    public ResponseEntity loves(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(page,pagesize,3);
        return ResponseEntity.ok(pageResult);
    }
  }
