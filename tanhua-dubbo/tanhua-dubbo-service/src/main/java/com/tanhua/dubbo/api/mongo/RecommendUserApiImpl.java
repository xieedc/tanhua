package com.tanhua.dubbo.api.mongo;


import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 今日佳人
 */
@Service
public class RecommendUserApiImpl implements RecommendUserApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 今日佳人
     * @param toUserId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long toUserId) {
        Criteria criteria = Criteria.where("toUserId").is(toUserId);
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Order.desc("score"))).limit(1);
        RecommendUser user = mongoTemplate.findOne(query, RecommendUser.class);
        return user;
    }

    /**
     * 首页推荐
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<RecommendUser> findPage(Integer page, Integer pagesize, Long userId) {
        //1.查询当前用户总推荐数量
        Query query = new Query(Criteria.where("toUserId").is(userId));
        long total = mongoTemplate.count(query, RecommendUser.class);
        //2.查询当前用户分页推荐数据
        query.limit(pagesize).skip((page-1)*pagesize);
        List<RecommendUser> recommendUserList = mongoTemplate.find(query,RecommendUser.class);
        //3.封装返回PageResult<RecommendUser>
        //方式1: setxx()
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        PageResult<RecommendUser> pageResult = new PageResult<>();
        pageResult.setCounts(total);
        pageResult.setPagesize((long)pagesize);
        pageResult.setPage((long)page);
        pageResult.setPages((long)pages);
        pageResult.setItems(recommendUserList);

       /* //方式二：构造方法
        PageResult<RecommendUser> pageResult =
                new PageResult(total,(long)pagesize,(long)page,(long)pages,recommendUserList);
*/
        return pageResult;
    }

    /**
     * 佳人信息 -查询两个用户的缘分值
     * @param userId
     * @param toUserId
     * @return
     */
    @Override
    public RecommendUser queryForScore(long userId, Long toUserId) {
        Query query = new Query(Criteria.where("userId").is(userId).
                and("toUserId").is(toUserId));
        query.with(Sort.by(Sort.Order.desc("date")));
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        return recommendUser;
    }
}
