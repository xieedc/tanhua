package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;

/**
 * 圈子服务接口
 */
public interface PublishApi {
    /**
     *圈子-发布动态
     * @param publishVo
     */
    void add(PublishVo publishVo);

    /**
     * 圈子-查询好友动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */

    PageResult<Publish> queryFriendPublishList(int page, int pagesize, Long userId);

    /**
     * 圈子-推荐动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Publish> queryRecommendPublishList(int page, int pagesize, Long userId);

    /**
     * 我的动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Publish> queryMyAlbum(int page, int pagesize, Long userId);

    /**
     * 单条动态查询
     * @param publishId
     * @return
     */
    Publish findById(String publishId);
}
