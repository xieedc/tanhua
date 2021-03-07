package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

/**
 * 动态评论服务接口（点赞、喜欢、评论）
 */

public interface CommentApi {
    /**
     * 动态-点赞
     * @param comment
     * @return
     */
    long sava(Comment comment);

    /**
     * 动态-取消点赞
     * @param comment
     * @return
     */
    long remove(Comment comment);


    /**
     * 评论列表
     * @param publishId
     * @return
     */
    PageResult<Comment> findPage(String publishId,int page, int pagesize);


    /**
     *点赞 评论  喜欢 列表查询
     *评论类型，1-点赞，2-评论，3-喜欢
     * @param page
     * @param pagesize
     * @param type
     * @param userId
     * @return
     */
    PageResult messageCommentList(Integer page, Integer pagesize, int type, Long userId);
}
