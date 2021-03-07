package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/smallVideos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 上传小视频
     * @param videoThumbnail
     * @param videoFile
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity post(MultipartFile videoThumbnail,MultipartFile videoFile) throws IOException {
        //视频封面 视频文件
        videoService.post(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询小视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity findPage(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
       PageResult<VideoVo> pageResult = videoService.findPage(page,pagesize);
       return ResponseEntity.ok(pageResult);
    }

    /**
     * 关注用户
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{uid}/userFocus",method = RequestMethod.POST)
    public ResponseEntity followUser(@PathVariable("uid") long userId){
        videoService.followUser(userId);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注用户
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{uid}/userUnFocus",method = RequestMethod.POST)
    public ResponseEntity unFollowUser(@PathVariable("uid") long userId){
        videoService.unFollowUser(userId);
        return ResponseEntity.ok(null);
    }
}
