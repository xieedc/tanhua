package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

public interface FriendApi {
    /**
     * 联系人添加
     * @param userId
     * @param friendId
     */
    void add(Long userId, Long friendId);

    /**
     * 联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    PageResult queryContacts(Integer page, Integer pagesize, String keyword, Long userId);
}
