package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.utils.IdService;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 小视频服务接口
 */
@Service
public class VideoApiImpl implements VideoApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    /**
     * 上传小视频
     * @param video
     */
    @Override
    public void save(Video video) {
        video.setId(ObjectId.get());//主键id 唯一值
        video.setVid(idService.nextId("video"));
        video.setCreated(System.currentTimeMillis());
        mongoTemplate.save(video);

    }

    /**
     * 查询小视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Video> findPage(int page, int pagesize) {
        PageResult<Video> pageResult = new PageResult<>();
        Query query = new Query();
        query.limit(pagesize).skip((page-1)*pagesize).with(Sort.by(Sort.Order.desc("created")));
        List<Video> videoList = mongoTemplate.find(query, Video.class);
        long total = mongoTemplate.count(query, Video.class);
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        pageResult.setCounts(total);
        pageResult.setPagesize((long) pagesize);
        pageResult.setPage((long) page);
        pageResult.setPages((long) pages);
        pageResult.setItems(videoList);
        return pageResult;
    }

    /**
     * 关注用户
     * @param userId
     * @param followUserId
     */
    @Override
    public void followUser(Long userId, long followUserId) {
        FollowUser followUser = new FollowUser();
        followUser.setUserId(userId);
        followUser.setFollowUserId(followUserId);
        followUser.setId(ObjectId.get());
        followUser.setCreated(System.currentTimeMillis());
        mongoTemplate.save(followUser);

    }

    /**
     * 取消关注用户
     * @param userId
     * @param followUserId
     */
    @Override
    public void unFollowUser(Long userId, long followUserId) {
       Query query = new Query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));
        mongoTemplate.remove(query,FollowUser.class);
    }
}
