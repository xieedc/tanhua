package com.tanhua.dubbo.utils;

import com.tanhua.domain.mongo.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class IdService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据集合名，生成自增id
     * @param collectinName
     * @return
     */
    public long nextId(String collectinName){
        Query query = Query.query(Criteria.where("collName").is(collectinName));
        Update update = new Update();
        update.inc("seqId",1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        Sequence seq = mongoTemplate.findAndModify(query, update, options, Sequence.class);
        return seq.getSeqId();
    }
}