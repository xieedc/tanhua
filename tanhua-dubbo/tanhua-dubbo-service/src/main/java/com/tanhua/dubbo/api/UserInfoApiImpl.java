package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.dao.UserInfoDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户信息管理业务处理类
 */
@Service
public class UserInfoApiImpl implements UserInfoApi{

    @Autowired
    private UserInfoDao userInfoDao;

    @Override
    public void save(UserInfo userInfo) {
        userInfoDao.insert(userInfo);
    }

    @Override
    public void update(UserInfo userInfo) {
        userInfoDao.updateById(userInfo);
    }

    @Override
    public UserInfo findByUserId(Long userId) {
        return userInfoDao.selectById(userId);
    }
}
