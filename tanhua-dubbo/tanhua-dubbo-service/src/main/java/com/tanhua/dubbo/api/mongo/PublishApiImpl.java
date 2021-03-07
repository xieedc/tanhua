package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.dubbo.utils.IdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * 圈子服务接收实现类
 */
@Service
@Slf4j
public class PublishApiImpl implements PublishApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    @Override
    public void add(PublishVo publishVo) {
        //统一获取vo数据
        Long userId = publishVo.getUserId();
        //  a.将数据保存发布表中
        Publish publish = new Publish();
        publish.setId(ObjectId.get());//发布表 主键id
        publish.setPid(idService.nextId("publish"));
        //将vo中数据 copy  到 publish
        BeanUtils.copyProperties(publishVo,publish);
        publish.setLocationName(publishVo.getLocation());//地理位置
        publish.setSeeType(1);//谁可以看，1-公开，2-私密，3-部分可见，4-不给谁看
        publish.setCreated(System.currentTimeMillis());//动态发布时间
        mongoTemplate.save(publish);
        //b.将数据保存相册表 quanzi_album_当前登录用户id  (我的动态表)
        Album album = new Album();
        album.setId(ObjectId.get());//主键id
        album.setPublishId(publish.getId());//发布id
        album.setCreated(publish.getCreated());//创建时间
        mongoTemplate.save(album, "quanzi_album_" + userId);
        //c.根据登录用户id查询好友表 是否好友
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        //d.循环往 时间线表 插入好友动态数据
        if (friendList != null && friendList.size() > 0) {
            for (Friend friend : friendList) {
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());//主键id
                timeLine.setUserId(userId);//当前登录用户 发布动态的用户id
                timeLine.setPublishId(publish.getId());//发布id
                timeLine.setCreated(publish.getCreated());
                mongoTemplate.save(timeLine, "quanzi_time_line_" + friend.getFriendId());
            }
        }
    }

    /**
     * 圈子- 查询好友动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<Publish> queryFriendPublishList(int page, int pagesize, Long userId) {
        PageResult<Publish> pageResult = new PageResult<>();
        //根据用户id分页查询时间线表
        Query query = new Query();
        query.limit(pagesize).skip((page - 1) * pagesize);
        query.with(Sort.by(Sort.Order.desc("created")));
        long total = mongoTemplate.count(query,"quanzi_time_line_" + userId);
        List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class, "quanzi_time_line_" + userId);
        List<Publish> publishList = new ArrayList<>();
        // 根据发布id 发布动态数据(发布表)
        for (TimeLine timeLine : timeLineList) {
            Query queryPublish = new Query(Criteria.where("id").is(timeLine.getPublishId()));
            Publish publish = mongoTemplate.findOne(queryPublish, Publish.class);
            if (publish != null)
                publishList.add(publish);
        }
        long pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        pageResult.setCounts(total);
        pageResult.setPagesize((long) pagesize);
        pageResult.setPage((long) page);
        pageResult.setPages((long) pages);
        pageResult.setItems(publishList);
        return pageResult;
    }

    /**
     * 圈子 -推荐动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<Publish> queryRecommendPublishList(int page, int pagesize, Long userId) {
        PageResult<Publish> pageResult = new PageResult<>();
        //根据用户id分页查询时间线表
        Query query = new Query(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page - 1) * pagesize);
        query.with(Sort.by(Sort.Order.desc("created")));
        long total = mongoTemplate.count(query, RecommendQuanzi.class);
        List<RecommendQuanzi> recommendQuanziList = mongoTemplate.find(query, RecommendQuanzi.class);
        List<Publish> publishList = new ArrayList<>();
        // 根据发布id 发布动态数据(发布表)
        for (RecommendQuanzi recommendQuanzi : recommendQuanziList) {
            Query queryPublish = new Query(Criteria.where("id").is(recommendQuanzi.getPublishId()));
            log.info("**************recommendQuanzi.getPublishId()*****************"+recommendQuanzi.getPublishId());
            Publish publish = mongoTemplate.findOne(queryPublish, Publish.class);
            if (publish != null)
                publishList.add(publish);
        }
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        pageResult.setCounts(total);
        pageResult.setPagesize((long) pagesize);
        pageResult.setPage((long) page);
        pageResult.setPages((long) pages);
        pageResult.setItems(publishList);
        return pageResult;
    }

    @Override
    public PageResult<Publish> queryMyAlbum(int page, int pagesize, Long userId) {
        PageResult<Publish> pageResult = new PageResult<>();
        //根据用户id分页查询相册表
        Query query = new Query();
        query.limit(pagesize).skip((page - 1) * pagesize);
        query.with(Sort.by(Sort.Order.desc("created")));
        long total = mongoTemplate.count(query, Album.class,"quanzi_album_"+userId);
        List<Album> albumList = mongoTemplate.find(query, Album.class,"quanzi_album_"+userId);
        List<Publish> publishList = new ArrayList<>();
        // 根据发布id 发布动态数据(发布表)
        for (Album album : albumList) {
            Query queryPublish = new Query(Criteria.where("id").is(album.getPublishId()));
            log.info("**************album.getPublishId()*****************"+album.getPublishId());
            Publish publish = mongoTemplate.findOne(queryPublish, Publish.class);
            if (publish != null)
                publishList.add(publish);
        }
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        pageResult.setCounts(total);
        pageResult.setPagesize((long) pagesize);
        pageResult.setPage((long) page);
        pageResult.setPages((long) pages);
        pageResult.setItems(publishList);
        return pageResult;
    }

    /**
     * 单条动态查询
     * @param publishId
     * @return
     */
    @Override
    public Publish findById(String publishId) {
        if(publishId.equals("visitors")){
            return null;
        }
        Query query = new Query(Criteria.where("id").is(new ObjectId(publishId)));
        return mongoTemplate.findOne(query,Publish.class);
    }
}
