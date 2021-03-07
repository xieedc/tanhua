package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;

public interface UserInfoApi {
    /**
     * 保存用户信息表
     */
    void save(UserInfo userInfo);

    /**
     * 根据用户id更新头像
     */
    void update(UserInfo userInfo);

    /**
     * 根据用户id查询用户信息对象
     */
    UserInfo findByUserId(Long userId);
}
