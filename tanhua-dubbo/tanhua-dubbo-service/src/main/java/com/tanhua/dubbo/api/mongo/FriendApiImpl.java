package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 好友服务接口实现
 */

@Service
public class FriendApiImpl implements FriendApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 联系人添加
     * @param userId
     * @param friendId
     */
    @Override
    public void add(Long userId, Long friendId) {
        Query query1 = new Query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        //分别保存两条好友记录
        if (!mongoTemplate.exists(query1, Friend.class)) {
            Friend friend1 = new Friend();
            friend1.setId(ObjectId.get());
            friend1.setUserId(userId);
            friend1.setFriendId(friendId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
        Query query2 = new Query(Criteria.where("userId").is(friendId).and("friendId").is(userId));
        if(!mongoTemplate.exists(query2,Friend.class)) {
            Friend friend2 = new Friend();
            friend2.setId(ObjectId.get());
            friend2.setUserId(friendId);
            friend2.setFriendId(userId);
            friend2.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend2);
        }
    }

    /**
     * 联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    @Override
    public PageResult queryContacts(Integer page, Integer pagesize, String keyword, Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page-1)*pagesize);
        //当前页面数据
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        //总记录数
        long total = mongoTemplate.count(query, Friend.class);
        int pages = total / pagesize + total % pagesize > 0 ? 1: 0;
        PageResult pageResult = new PageResult(total,(long) pagesize,(long)page,(long)pages,friendList);
        return pageResult;
    }
}
