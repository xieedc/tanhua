package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitor;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 谁看过我服务接口实现类
 */
@Service
public class VisitorApiImpl implements visitorApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据date>上次访问时间+userId 查询前5条
     * @param time
     * @param userId
     * @return
     */
    @Override
    public List<Visitor> queryVisitors(String time, Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("date").gt(time)).limit(5);
        return mongoTemplate.find(query,Visitor.class);
    }

    /**
     * 直接当前用户id查询前5条
     * @param userId
     * @return
     */
    @Override
    public List<Visitor> queryVisitors(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId)).limit(5);
        return mongoTemplate.find(query,Visitor.class);
    }

    /**
     * 保存访客记录
     * @param visitor
     */
    @Override
    public void save(Visitor visitor) {
        visitor.setId(ObjectId.get());
        visitor.setDate(System.currentTimeMillis());
        mongoTemplate.save(visitor);
    }
}
