package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

/**
 * 互相喜欢、喜欢、粉丝服务接口
 */

public interface userLikeApi {

    /**
     * 统计好友数 即相互喜欢的个数
     * @param userId
     * @return
     */
    Long countLikeEachOther(Long userId);

    /**
     * 统计我喜欢的个数
     * @param userId
     * @return
     */
    Long countOneSideLike(Long userId);

    /**
     * 统计我的粉丝个数
     * @param userId
     * @return
     */
    Long countFens(Long userId);

    /**
     * //1 互相关注
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageLikeEachOther(Long userId, int page, int pagesize);

    /**
     * //2 我关注
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageOneSideLike(Long userId, int page, int pagesize);

    /**
     * //3 粉丝
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageFens(Long userId, int page, int pagesize);

    /**
     * //4 谁看过我
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageMyVisitors(Long userId, int page, int pagesize);

    /**
     *粉丝喜欢
     * @param userId
     * @param fansUserId
     */
    void delete(Long userId, Long fansUserId);
}
