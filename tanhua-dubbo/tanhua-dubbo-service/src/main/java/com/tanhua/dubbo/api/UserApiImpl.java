package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.dao.UserDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
/**
 * 用户服务实现类
 */

@Service
@Transactional
public class UserApiImpl implements UserApi{
    @Autowired
    private UserDao userDao;
    @Override
    public User findByMobile(String mobile) {
        //创建条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile",mobile);
        return userDao.selectOne(queryWrapper);
    }

    /**
     * 保存用户 返回用户id(后续要用)
     * @param user
     */
    @Override
    public Long saveUser(User user) {
        user.setCreated(new Date());
        user.setUpdated(new Date());
        userDao.insert(user);
        return user.getId();
    }
}
