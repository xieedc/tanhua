package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论管理业务层
 */
@Service
public class CommentService {

    @Reference
    private CommentApi commentApi;


    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 评论列表
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<CommentVo> findPage(String publishId, int page, int pagesize) {
        //定义返回vo
        PageResult<CommentVo> voPageResult = new PageResult<>();
        //根据发布id commentType=2(1-点赞，2-评论，3-喜欢)  pubType=1(1-对动态操作 2-对视频操作 3-对评论操作)
        PageResult<Comment> commentPageResult = commentApi.findPage(publishId,page,pagesize);
        List<Comment> commentList = commentPageResult.getItems();

        //定义list<vo>
        List<CommentVo> commentVoList = new ArrayList<>();
        if (commentList != null && commentList.size()>0) {
            for (Comment comment : commentList) {
                //定义返回的CommentVo
                CommentVo commentVo = new CommentVo();

                Long userId = comment.getUserId();
                UserInfo userInfo = userInfoApi.findByUserId(userId);
                //将comment userInfo copy 到CommentVo
                BeanUtils.copyProperties(comment,commentVo);
                BeanUtils.copyProperties(userInfo,commentVo);
                //设置评论id
                commentVo.setId(comment.getId().toHexString());
                commentVo.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
                //commentVo.setHasLiked(0);//是否点赞（1是，0否）
                String key = "comment_like_" + comment.getUserId()+"_" + comment.getPublishId().toHexString();
                 // 记录下点了赞了
                if(redisTemplate.hasKey(key)){
                    commentVo.setHasLiked(1);//是否点赞
                }
                else {
                    commentVo.setHasLiked(0);//是否点赞
                }

                commentVoList.add(commentVo);
            }
        }
        BeanUtils.copyProperties(commentPageResult,voPageResult);
        //设置分页评论具体数据
        voPageResult.setItems(commentVoList);
        return voPageResult;

    }

    /**
     * 动态-评论
     * @param publishId
     * @param content
     */
    public void add(String publishId, String content) {

        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(2);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setContent(content);//评论内容
        comment.setUserId(UserHolder.getUserId());//当前登录用户id  评论人
        commentApi.sava(comment);
    }

    /**
     * 评论-点赞
     * @param commentId
     * @return
     */
    public long like(String commentId) {
        Comment comment = new Comment();
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(UserHolder.getUserId());//点赞用户
        comment.setPublishId(new ObjectId(commentId));//评论id
        //1.保存点赞记录到comment中
        long tatal = commentApi.sava(comment);
        return tatal;
    }

    /**
     * 评论-取消点赞
     * @param commentId
     * @return
     */
    public long dislike(String commentId) {
        Comment comment = new Comment();
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(UserHolder.getUserId());//点赞用户
        comment.setPublishId(new ObjectId(commentId));//评论id
        //1.保存点赞记录到comment中
        long tatal = commentApi.remove(comment);
        return tatal;
    }
}
