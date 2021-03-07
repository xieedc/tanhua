package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service
@Slf4j
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 动态点赞
     * @param comment
     * @return
     */
    @Override
    public long sava(Comment comment) {
        //a.动态点赞记录保持 动态评论表
        comment.setId(ObjectId.get());
        comment.setCreated(System.currentTimeMillis());
        //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        if(comment.getPubType()==1) {
            Query query = new Query(Criteria.where("id").is(comment.getPublishId()));
            //根据publishId 到动态发布表中 查询发布userId
            Publish publish = mongoTemplate.findOne(query, Publish.class);
            comment.setPublishUserId(publish.getUserId());//被评论人ID
        }
        mongoTemplate.save(comment);
        //b.根据发布id 更新发布表点赞数+1
        updateCount(comment, 1);
        //c.根据发布id查询发布表点赞数
        long count = getCount(comment);
        return count;
    }


    /**
     * 动态-取消点赞
     */

    @Override
    public long remove(Comment comment) {
        //a.动态点赞记录从动态评论表删除
        Query query = new Query(
                Criteria.where("publishId").is(comment.getPublishId()).
                        and("commentType").is(comment.getCommentType()).
                        and("pubType").is(comment.getPubType()).
                        and("userId").is(comment.getUserId())
        );
        mongoTemplate.remove(query, Comment.class);

        //b.根据发布id 更新发布表点赞数-1
        updateCount(comment, -1);
        //c.根据发布id查询发布表点赞数
        Long count = getCount(comment);
        return count;
    }

    /**
     * 评论列表
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Comment> findPage(String publishId, int page, int pagesize) {
        PageResult<Comment> pageResult = new PageResult<>();
        //据发布id commentType=2(1-点赞，2-评论，3-喜欢)
        // pubType=1(1-对动态操作 2-对视频操作 3-对评论操作)
        Query query = new Query(Criteria.
                where("publishId").is(new ObjectId(publishId)).
                and("commentType").is(2).and("pubType").is(1));
        query.limit(pagesize).skip((page-1)*pagesize).with(Sort.by(Sort.Order.desc("created")));
        List<Comment> commentList = mongoTemplate.find(query,Comment.class);
        long total = mongoTemplate.count(query,Comment.class);
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        pageResult.setCounts(total);
        pageResult.setPagesize((long) pagesize);
        pageResult.setPage((long) page);
        pageResult.setPages((long) pages);
        pageResult.setItems(commentList);
        return pageResult;
    }

    /**
     * 点赞 评论  喜欢 列表查询
     * 评论类型，1-点赞，2-评论，3-喜欢
     * @param page
     * @param pagesize
     * @param type
     * @param userId
     * @return
     */
    @Override
    public PageResult messageCommentList(Integer page, Integer pagesize, int type, Long userId) {
        //query：select * from comment where type =1 and userId = 10004 limit 0,10
        Query query = new Query(Criteria.where("commentType").is(type).and("publishUserId").is(userId));
        query.limit(pagesize).skip((page-1)*pagesize);
        //分页数据
        List<Comment> commentList = mongoTemplate.find(query, Comment.class);
        //总记录数
        long total = mongoTemplate.count(query, Comment.class);
        int pages = total / pagesize + total % pagesize > 0 ? 1 : 0;
        PageResult pageResult =
                new PageResult(total,(long)pagesize,(long)page,(long)pages,commentList);
        return pageResult;
    }

    /**
     *更新动态发布表中点赞数（点赞+1 取消点赞-1）
     * 更新评论数
     * 更新喜欢数
     * @param comment
     * @param inc
     */
    private void updateCount(Comment comment, int inc) {


        //更新条件
        Query query = new Query(Criteria.where("id").is(comment.getPublishId()));
        //更新数据
        Update update = new Update();
        update.inc(comment.getCol(), inc);
        Class<?> cls = Publish.class;//默认针对动态发布表操作
        if (comment.getPubType() == 3) {
            cls = Comment.class;
            log.info("***Comment*******updateCount******getPublishId{}****inc{}****", comment.getPublishId(), inc);
        }
        mongoTemplate.updateFirst(query, update, cls);
    }


    /**
     * 根据发布id 到动态发布表查询 点赞数
     *
     * @param comment
     * @return
     */
    private long getCount(Comment comment) {
        //构造条件
        Query query = new Query(Criteria.where("id").is(comment.getPublishId()));
        //动态发布表
        if (comment.getPubType() == 1) {
            Publish publish = mongoTemplate.findOne(query, Publish.class);
            if (comment.getCommentType() == 1) {// //评论类型，1-点赞，2-评论，3-喜欢
                return (long) publish.getLikeCount();
            }
            if (comment.getCommentType() == 2) {// //评论类型，1-点赞，2-评论，3-喜欢
                return (long) publish.getCommentCount();
            }
            if (comment.getCommentType() == 3) {// //评论类型，1-点赞，2-评论，3-喜欢
                return (long) publish.getLoveCount();
            }

        }
        //动态评论表
        if (comment.getPubType() == 3) {
            log.info("***Comment*******getCount******getPublishId{}****", comment.getPublishId());
            Comment cm = mongoTemplate.findOne(query, Comment.class);
            return (long) cm.getLikeCount();
        }
        return 99l;
    }

}