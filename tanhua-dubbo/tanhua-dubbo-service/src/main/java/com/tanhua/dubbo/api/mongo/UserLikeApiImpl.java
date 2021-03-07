package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * 互相喜欢、喜欢、粉丝服务接口实现类
 */
@Service
public class UserLikeApiImpl implements userLikeApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 统计好友数 即相互喜欢的个数
     * @param userId
     * @return
     */
    @Override
    public Long countLikeEachOther(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 统计我喜欢的个数
     * @param userId
     * @return
     */
    @Override
    public Long countOneSideLike(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 统计我的粉丝个数
     * @param userId
     * @return
     */
    @Override
    public Long countFens(Long userId) {
        Query query = new Query(Criteria.where("likeUserId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * //1 互相关注
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageLikeEachOther(Long userId, int page, int pagesize) {
        //1.根据userId查询好友表 得到好友ids(好友列表 以及 总记录数)
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        long total = mongoTemplate.count(query, Friend.class);
        //2.根据好友ids 与 当前用户id  再推荐用户表recommendUser得到缘分值
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(friendList != null && friendList.size()>0){
            for (Friend friend : friendList) {
                recommendUserList.add(queryScore(friend.getFriendId(), userId));
            }
        }
        //3.封装pageResult
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        //定义返回PageResult<RecommendUser>
        PageResult pageResult =
                new PageResult(total,(long)pagesize,(long)page,(long)pages,recommendUserList);
        return pageResult;
    }

    /**
     * 公共查看缘分值方法
     * @param userId
     * @param toUserId
     * @return
     */
    private RecommendUser queryScore(Long userId, Long toUserId) {
        Query query = Query.query(Criteria.where("toUserId").is(toUserId).and("userId").is(userId));
        RecommendUser user = this.mongoTemplate.findOne(query, RecommendUser.class);
        if (user == null) {
            user = new RecommendUser();
            user.setUserId(userId);
            user.setToUserId(toUserId);
            user.setScore(95d);
        }
        return user;
    }


    /**
     * //2 我关注
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageOneSideLike(Long userId, int page, int pagesize) {
        //1.根据userId查询喜欢表 得到喜欢ids(喜欢列表 以及 总记录数)
        Query query = new Query(Criteria.where("userId").is(userId));
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        long total = mongoTemplate.count(query, UserLike.class);
        //2.根据喜欢ids 与 当前用户id  再推荐用户表recommendUser得到缘分值
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(userLikeList != null && userLikeList.size()>0){
            for (UserLike userLike : userLikeList) {
                recommendUserList.add(queryScore(userLike.getLikeUserId(), userId));
            }
        }
        //3.封装pageResult
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        //定义返回PageResult<RecommendUser>
        PageResult pageResult =
                new PageResult(total,(long)pagesize,(long)page,(long)pages,recommendUserList);
        return pageResult;
    }


    /**
     * //3 粉丝
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageFens(Long userId, int page, int pagesize) {
        //1.根据userId查询喜欢表 得到喜欢ids(喜欢列表 以及 总记录数)
        Query query = new Query(Criteria.where("likeUserId").is(userId));
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        long total = mongoTemplate.count(query, UserLike.class);
        //2.根据喜欢ids 与 当前用户id  再推荐用户表recommendUser得到缘分值
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(userLikeList != null && userLikeList.size()>0){
            for (UserLike userLike : userLikeList) {
                recommendUserList.add(queryScore(userLike.getUserId(),userId));
            }
        }
        //3.封装pageResult
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        //定义返回PageResult<RecommendUser>
        PageResult pageResult =
                new PageResult(total,(long)pagesize,(long)page,(long)pages,recommendUserList);
        return pageResult;
    }

    /**
     * 谁看过我
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageMyVisitors(Long userId, int page, int pagesize) {
        //1.根据userId查询好友表 得到好友ids(好友列表 以及 总记录数)
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
        long total = mongoTemplate.count(query, Visitor.class);
        //2.根据好友ids 与 当前用户id  再推荐用户表recommendUser得到缘分值
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(visitorList != null && visitorList.size()>0){
            for (Visitor visitor : visitorList) {
                recommendUserList.add(queryScore(visitor.getVisitorUserId(),userId));
            }
        }
        //3.封装pageResult
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        //定义返回PageResult<RecommendUser>
        PageResult pageResult =
                new PageResult(total,(long)pagesize,(long)page,(long)pages,recommendUserList);
        return pageResult;
    }

    /**
     * 根据当前用户id 和 粉丝的用户id 删除UserLike中 粉丝喜欢记录
     * @param userId
     * @param fansUserId
     */
    @Override
    public void delete(Long userId, Long fansUserId) {
        Query query = new Query(Criteria.where("userId").is(fansUserId).and("likeUserId").is(userId));
        mongoTemplate.remove(query,UserLike.class);
    }
}
