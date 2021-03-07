package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {



    /**
     * 上报地理信息
     * @param latitude
     * @param longitude
     * @param addrStr
     */
    void addLocation(Double latitude, Double longitude, String addrStr,Long userId);

    /**
     * 根据搜索距离和当前用户id 搜索附近用户信息
     * withinSphere:地理位置搜索
     * @param userId
     * @param distance
     * @return
     */
    List<UserLocationVo> searchNearBy(Long userId, long distance);
}
