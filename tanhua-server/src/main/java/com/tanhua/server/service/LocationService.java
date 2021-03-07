package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.UserLocationVo;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 搜附近业务层处理
 */
@Service
public class LocationService {


    @Reference
    private UserLocationApi userLocationApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上报地理位置信息
     * @param map
     */
    public void addLocation(Map<String, Object> map) {
        Double latitude = (Double) map.get("latitude");//维度
        Double longitude = (Double) map.get("longitude");//经度
        String addrStr = (String) map.get("addrStr");
        Long userId = UserHolder.getUserId();
        //直接调用上传地址信息服务方法
        userLocationApi.addLocation(latitude,longitude,addrStr,userId);

    }

    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> searchNearBy(String gender, String distance) {
        List<NearUserVo> nearUserVoList = new ArrayList<>();
        Long userId = UserHolder.getUserId();
        // 调用服务根据搜索距离和当前用户id 搜索附近用户信息（包含不需要的数据：当前用户数据+不需的性别数据）
        //   注意：调用mongodb得到List<UserLocation>不能直接返回给消费者，需要通过UserLocationVo转换
        List<UserLocationVo> userLocationList = userLocationApi.searchNearBy(userId,Long.parseLong(distance));
        //   循环遍历List<UserLocationVo> 需要将性别条件不符合要求的数据排除 、 排除当前用户数据
        if (userLocationList != null && userLocationList.size()>0){
            for (UserLocationVo userLocationVo : userLocationList) {
                NearUserVo nearUserVo = new NearUserVo();
                Long nearUserId = userLocationVo.getUserId();//附近用户id
                if (userId.toString().equals(nearUserId.toString())){//排除当前用户数据
                    continue;
                }

                UserInfo userInfo = userInfoApi.findByUserId(nearUserId);
                if (!gender.equals(userInfo.getGender())){
                    continue;
                }
                nearUserVo.setUserId(nearUserId);
                nearUserVo.setAvatar(userInfo.getAvatar());
                nearUserVo.setNickname(userInfo.getNickname());
                nearUserVoList.add(nearUserVo);
            }
        }
        //返回list<vo>
        return nearUserVoList;
    }

}
