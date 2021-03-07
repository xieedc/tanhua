package com.tanhua.server.controller;

import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.server.service.IMService;
import com.tanhua.server.service.LocationService;
import com.tanhua.server.service.TodayBestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 今日佳人控制层
 */
@RestController
@RequestMapping("/tanhua")
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;

    @Autowired
    private IMService imService;

    @Autowired
    private LocationService locationService;

    /**
     * 今日佳人
     */
    @RequestMapping(value = "/todayBest",method = RequestMethod.GET)
    public ResponseEntity todayBest(){
        TodayBestVo todayBestVo = todayBestService.todayBest();
        return ResponseEntity.ok(todayBestVo);
    }
    /**
     * 首页推荐
     */
    @RequestMapping(value = "recommendation",method = RequestMethod.GET)
    public ResponseEntity recommendation(RecommendUserQueryParam queryParam){
        PageResult<TodayBestVo> pageResult = todayBestService.recommendation(queryParam);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 佳人信息
     */
    @RequestMapping(value = "/{id}/personalInfo",method = RequestMethod.GET)
    public ResponseEntity queryUserDetail(@PathVariable("id") long userId){
        //userId佳人用户id
        TodayBestVo todayBestVo = todayBestService.queryUserDetail(userId);
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 查询陌生人问题
     * @param userId
     * @return
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.GET)
    public ResponseEntity strangerQuestions (long userId){
        String content = todayBestService.strangerQuestions(userId);
        return ResponseEntity.ok(content);
    }

    /**
     * 回复陌生人信息
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.POST)
    public ResponseEntity replyStrangerQuestions(@RequestBody Map<String,Object> paramMap){
        imService.replyStrangerQuestions(paramMap);
        return ResponseEntity.ok(null);
    }

    /**
     *
     * @param gender
     * @param distance
     * @return
     */
    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public ResponseEntity searchNearBy(@RequestParam("gender") String gender,@RequestParam("distance") String distance){
        List<NearUserVo> nearUserVoList = locationService.searchNearBy(gender,distance);
        return ResponseEntity.ok(nearUserVoList);

    }
}
