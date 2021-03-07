package com.tanhua.server.controller;

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论管理控制层
 */
@RestController
@RequestMapping("/comments")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 评论列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity findPage(String movementId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult<CommentVo> pageResult = commentService.findPage(movementId, page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态-评论
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity add(@RequestBody Map<String,String> params){
        String publishId = params.get("movementId");//发布id
        String comment = params.get("comment");//发布的内容
        commentService.add(publishId,comment);
        return ResponseEntity.ok(null);
    }

    /**
     * 评论-点赞
     * @param commentId
     * @return
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String commentId ){
        long total = commentService.like(commentId);
        return ResponseEntity.ok(total);
    }

    /**
     * 评论-取消点赞
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String commentId ){
        log.info("**********dislike*******commentId*********{}**********",commentId);
        long total = commentService.dislike(commentId);
        return ResponseEntity.ok(total);
    }
}