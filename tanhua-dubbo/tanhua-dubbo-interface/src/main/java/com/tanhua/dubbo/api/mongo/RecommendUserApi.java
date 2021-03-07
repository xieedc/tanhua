package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * 今日佳人
 */
public interface RecommendUserApi {
    /**
     * 今日佳人
     * @param toUserId
     * @return
     */
     RecommendUser queryWithMaxScore(Long toUserId);

    /**
     * 首页推荐
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<RecommendUser> findPage(Integer page, Integer pagesize, Long userId);

    /**
     * 佳人信息 查询两个用户的缘分值
     * @param userId
     * @param toUserId
     * @return
     */
    RecommendUser queryForScore(long userId, Long toUserId);
}
