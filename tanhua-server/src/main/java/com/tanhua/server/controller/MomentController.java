package com.tanhua.server.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.server.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class MomentController {
    @Autowired
    private MomentService momentService;

    /**
     * 圈子-发布动态
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity postMoment(PublishVo publishVo, MultipartFile[] imageContent) throws IOException {
        momentService.postMoment(publishVo,imageContent);
        return ResponseEntity.ok(null);
    }

    /**
     * 圈子-查询好友动态
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity queryFriendPublishList(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MomentVo> pageResult = momentService.queryFriendPublishList(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 圈子-推荐动态
     */
    @RequestMapping(value = "/recommend",method = RequestMethod.GET)
    public ResponseEntity queryRecommendPublishList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MomentVo> pageResult = momentService.queryFriendPublishList(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 我的动态
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseEntity queryMyAlbum(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pagesize,Long userId){
        PageResult<MomentVo> pageResult = momentService.queryMyAlbum(page,pagesize,userId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态-点赞
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id")String publishId){
        long total = momentService.like(publishId);
        return ResponseEntity.ok(total);
    }

    /**
     * 动态-取消点赞
     */
    @RequestMapping(value = "{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id")String publishId){
        long total = momentService.dislike(publishId);
        return ResponseEntity.ok(total);
    }

    /**
     * 动态-喜欢
     * @param publishId
     * @return
     */
    @RequestMapping(value = "{id}/love",method = RequestMethod.GET)
    public ResponseEntity love (@PathVariable("id")String publishId){
        long total = momentService.love(publishId);
        return ResponseEntity.ok(total);
    }

    /**
     * 动态-取消喜欢
     * @param publishId
     * @return
     */
    @RequestMapping(value = "/{id}/unlove",method = RequestMethod.GET)
    public ResponseEntity unlove(@PathVariable("id")String publishId){
        long total = momentService.unlove(publishId);
        return ResponseEntity.ok(total);
    }

    /**
     * 单条动态查询
     * @param publish
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity queryById(@PathVariable("id")String publish){
        MomentVo momentVo = momentService.queryById(publish);
        return ResponseEntity.ok(momentVo);
    }

    /**
     * 谁看过我（单条动态查询代码不需注释了， 今日佳人功能也能正常看到了）
     */
    @RequestMapping(value = "/visitors",method = RequestMethod.GET)
    public ResponseEntity queryVisitors(){
        List<VisitorVo> list = momentService.queryVisitors();
        return ResponseEntity.ok(list);
    }


}
