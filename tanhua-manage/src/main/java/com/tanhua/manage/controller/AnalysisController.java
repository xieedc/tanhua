package com.tanhua.manage.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tanhua.manage.service.AnalysisService;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 首页-统计分析
     * @return
     */
    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ResponseEntity getSummary() {
        //正确：new DateTime();
        DateTime dateTime = DateUtil.parseDate("2020-09-08");
        //创建vo
        AnalysisSummaryVo vo = new AnalysisSummaryVo();
        vo.setCumulativeUsers(analysisService.queryNumRegistered());//累计用户数
        vo.setActivePassMonth(analysisService.queryActiveUserCount(dateTime, -30));//过去30天活跃用户数
        vo.setActivePassWeek(analysisService.queryActiveUserCount(dateTime, -7));//过去7天活跃用户
        vo.setNewUsersToday(analysisService.queryRegisterUserCount(dateTime, 0));//今日新增用户数量
        vo.setNewUsersTodayRate(
                ComputeUtil.computeRate(
                        analysisService.queryRegisterUserCount(dateTime, -1),
                        analysisService.queryRegisterUserCount(dateTime, 0)
                )
        );//今日新增用户涨跌率，单位百分数，正数为涨，负数为跌
        // 获取今日注册用户数 / 昨天注册用户数
        vo.setLoginTimesToday(analysisService.queryLoginUserCount(dateTime, 0));//今日登录次数
        vo.setLoginTimesTodayRate(
                ComputeUtil.computeRate(
                        analysisService.queryLoginUserCount(dateTime, -1),
                        analysisService.queryLoginUserCount(dateTime, 0)
                )
        );//今日登录次数涨跌率，单位百分数，正数为涨，负数为跌
        vo.setActiveUsersToday(analysisService.queryActiveUserCount(dateTime, 0));//今日活跃用户数量
        vo.setActiveUsersTodayRate(
                ComputeUtil.computeRate(
                        analysisService.queryActiveUserCount(dateTime, -1),
                        analysisService.queryActiveUserCount(dateTime, 0)
                )
        );//今日活跃用户涨跌率，单位百分数，正数为涨，负数为跌
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/users")
    public AnalysisUsersVo getUsers(@RequestParam(name = "sd") Long sd
            , @RequestParam("ed") Long ed
            , @RequestParam("type") Integer type) {
        return this.analysisService.queryAnalysisUsersVo(sd, ed, type);
    }
}