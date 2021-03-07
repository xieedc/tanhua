package com.tanhua.server.service;

import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 今日佳人业务层处理
 */
@Service
public class TodayBestService {

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    /**
     * 今日佳人
     */
    public TodayBestVo todayBest() {
        TodayBestVo vo = new TodayBestVo();
        Long toUserId = UserHolder.getUserId();
        //a.根据当前登录用户id查询今日佳人集合
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(toUserId);
        //b.如果获取不到集合,设置默认数据
        if (recommendUser == null){
            recommendUser = new RecommendUser();
            recommendUser.setScore(99d);
            recommendUser.setUserId(1l);
        }
        //c.根据今日佳人中userId 到UserInfo中查询用户信息
        UserInfo userInfo = userInfoApi.findByUserId(recommendUser.getUserId());
        BeanUtils.copyProperties(userInfo,vo);
        if (StringUtils.isNotEmpty(userInfo.getTags())) {
            String[] splitTags = userInfo.getTags().split(",");
            vo.setTags(splitTags);
        }
        //缘分值
        vo.setFateValue(recommendUser.getScore().longValue());
        return vo;
    }

    /**
     *  首页推荐
     * @param queryParam
     * @return
     */
    public PageResult<TodayBestVo> recommendation(RecommendUserQueryParam queryParam) {
        //用户当前登录用户id
        Long userId = UserHolder.getUserId();
        Integer page = queryParam.getPage();
        Integer pagesize = queryParam.getPagesize();
        //定义vo返回
        PageResult<TodayBestVo> voPageResult = new PageResult<>();
        //1.根据条件查询推荐用户表 得到PageResult<RecommendUser> 再得到List<RecommendUser>
        PageResult<RecommendUser> pageResult =  recommendUserApi.findPage(page,pagesize,userId);
        List<RecommendUser> recommendUserList = pageResult.getItems();
        //2.如果没有数据设置默认数据
        if (recommendUserList == null || recommendUserList.size() == 0){
            //设置默认数据
            voPageResult = new PageResult(10l, queryParam.getPagesize().longValue(), 1l, 1l, null);
            recommendUserList = defaultRecommend();
        }
        //3.根据userIds查询List<UserInfo>
        //将List<RecommendUser> recommendUserList 转为List<TodayBestVo>
        ArrayList<TodayBestVo> todayBestVoList = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUserList) {
            TodayBestVo todayBestVo = new TodayBestVo();
            UserInfo userInfo = userInfoApi.findByUserId(recommendUser.getUserId());
            BeanUtils.copyProperties(userInfo,todayBestVo);
            //设置缘分值
            todayBestVo.setFateValue(recommendUser.getScore().longValue());
            //设置tags标签
            if (StringUtils.isNotEmpty(userInfo.getTags())){
                todayBestVo.setTags(userInfo.getTags().split(","));
            }
            todayBestVoList.add(todayBestVo);
        }
        //转为vo
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(todayBestVoList);
        return voPageResult;
    }

    //构造默认数据
    private List<RecommendUser> defaultRecommend() {
        String ids = "1,2,3,4,5,6,7,8,9,10";
        List<RecommendUser> records = new ArrayList<>();
        for (String id : ids.split(",")) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(Long.valueOf(id));
            recommendUser.setScore(RandomUtils.nextDouble(70,98));

        }
        return records;
    }

    /**
     * 佳人信息
     * @param userId
     * @return
     */
    public TodayBestVo queryUserDetail(long userId) {
        //返回vo
        TodayBestVo todayBestVo = new TodayBestVo();
        //1根据当前传入用户id查询用户信息userInfo表
        UserInfo userInfo = userInfoApi.findByUserId(userId);
        //2根据传入的用户id和当前登录的用户id 查询推荐用户表 得到缘分值
        Long toUserId = UserHolder.getUserId();
        RecommendUser recommendUser = recommendUserApi.queryForScore(userId,toUserId);
        //copy
        BeanUtils.copyProperties(userInfo,todayBestVo);
        //tags
        if (StringUtils.isNotEmpty(userInfo.getTags())) {
            todayBestVo.setTags(userInfo.getTags().split(","));
        }
        //设置缘分值
        todayBestVo.setFateValue(recommendUser.getScore().longValue());
        //返回vo
        return todayBestVo;
    }

    /**
     * 查询陌生人问题
     * @param userId
     * @return
     */
    public String strangerQuestions(long userId) {
        //根据当前登录用户id查询问题表 得到问题
        Question question = questionApi.findByUserId(userId);
        if (question == null) {
            return "今晚约吗";
        }
        //返回陌生人问题
        return question.getTxt();
    }
}
