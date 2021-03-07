package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

/**
 * 小视频服务接口
 */
public interface VideoApi {

    /**
     * 上传小视频
     * @param video
     */
    void save(Video video);

    /**
     * 分页查询视频列表数据
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Video> findPage(int page, int pagesize);

    /**
     * 关注用户
     * @param userId
     * @param followUserId
     */
    void followUser(Long userId, long followUserId);


    /**
     * 取消关注用户
     * @param userId
     * @param followUserId
     */
    void unFollowUser(Long userId, long followUserId);
}
