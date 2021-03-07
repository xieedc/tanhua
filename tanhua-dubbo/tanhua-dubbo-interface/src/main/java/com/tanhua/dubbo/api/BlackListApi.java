package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;

/**
 * 黑名单列表
 */
public interface BlackListApi {

    /**
     * 查询黑名单列表
     */
    PageResult<UserInfo> findBlackList(int page, int pagesize, Long userId);


    /**
     * 移除黑名单
     * @param deleteUserId
     */
    void delBlacklist(String deleteUserId, Long userId);
}
