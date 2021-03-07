package com.tanhua.manage.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计分析业务处理层
 */
@Service
public class AnalysisService extends ServiceImpl<AnalysisByDayMapper, AnalysisByDay> {

    /**
     * 累计用户数
     * SELECT SUM(num_registered) AS numRegistered FROM tb_analysis_by_day
     * @return
     */
    public Long queryNumRegistered() {
        //SUM(num_registered):数据库字段 numRegistered：属性或字段名
        AnalysisByDay analysisByDay = getOne(Wrappers.<AnalysisByDay>query().select("SUM(num_registered) as numRegistered"));
        return analysisByDay.getNumRegistered().longValue();
    }

    /**
     * 查询活跃用户的数量
     */
    public Long queryActiveUserCount(DateTime today, int offset) {
        return this.queryUserCount(today, offset, "num_active");
    }

    /**
     * 查询注册用户的数量
     */
    public Long queryRegisterUserCount(DateTime today, int offset) {
        return this.queryUserCount(today, offset, "num_registered");
    }

    /**
     * 查询登录用户的数量
     */
    public Long queryLoginUserCount(DateTime today, int offset) {
        return this.queryUserCount(today, offset, "num_login");
    }
    /**
     * 创建公共方法
     */
    private Long queryUserCount(DateTime today, int offset, String column){
        AnalysisByDay analysisByDay = getOne(Wrappers.<AnalysisByDay>query()
                .select("SUM(" + column + ") as numRegistered")
                .le("record_date",today.toDateStr()) //<='2020-09-18'
                .ge("record_date", DateUtil.offsetDay(today,offset).toDateStr()));//>='xxxx-xxx-xx'
        return analysisByDay.getNumRegistered().longValue();
    }

    /**
     * 新增、活跃用户、次日留存率
     */
    public AnalysisUsersVo queryAnalysisUsersVo(Long sd, Long ed, Integer type) {

        DateTime startDate = DateUtil.date(sd);

        DateTime endDate = DateUtil.date(ed);

        AnalysisUsersVo analysisUsersVo = new AnalysisUsersVo();

        //今年数据
        analysisUsersVo.setThisYear(this.queryDataPointVos(startDate, endDate, type));
        //去年数据
        analysisUsersVo.setLastYear(this.queryDataPointVos(
                DateUtil.offset(startDate, DateField.YEAR, -1),
                DateUtil.offset(endDate, DateField.YEAR, -1), type)
        );

        return analysisUsersVo;
    }

    private List<DataPointVo> queryDataPointVos(DateTime sd, DateTime ed, Integer type) {

        String startDate = sd.toDateStr();

        String endDate = ed.toDateStr();

        String column = null;
        switch (type) { //101 新增 102 活跃用户 103 次日留存率
            case 101:
                column = "num_registered";
                break;
            case 102:
                column = "num_active";
                break;
            case 103:
                column = "num_retention1d";
                break;
            default:
                column = "num_active";
                break;
        }

        List<AnalysisByDay> analysisByDayList = super.list(Wrappers.<AnalysisByDay>query()
                .select("record_date , " + column + " as num_active")
                .ge("record_date", startDate)
                .le("record_date", endDate));

        return analysisByDayList.stream()
                .map(analysisByDay -> new DataPointVo(DateUtil.date(analysisByDay.getRecordDate()).toDateStr(), analysisByDay.getNumActive().longValue()))
                .collect(Collectors.toList());
    }
}
