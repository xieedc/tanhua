package com.tanhua.dubbo.api.mongo;


import com.tanhua.domain.mongo.Visitor;

import java.util.List;

/**
 * 谁看过我的服务接口
 */
public interface visitorApi {

    /**
     * 根据date>上次访问时间+userId 查询前5条
     * @param time
     * @param userId
     * @return
     */
    List<Visitor> queryVisitors(String time, Long userId);

    /**
     * 直接当前用户id查询前5条
     * @param userId
     * @return
     */
    List<Visitor> queryVisitors(Long userId);

    /**
     * 保存访客记录
     */
    void save(Visitor visitor);

}
