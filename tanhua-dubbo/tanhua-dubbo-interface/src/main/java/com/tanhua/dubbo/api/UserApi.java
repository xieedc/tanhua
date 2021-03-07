package com.tanhua.dubbo.api;
import com.tanhua.domain.db.User;

/**
 * 服务提供者接口
 *
 */
public interface UserApi {

    /**
     * 根据手机号码 查询 用户对象
     */
    User findByMobile(String mobile);

    /**
     * 保存用户
     */
    Long saveUser(User user);
}
