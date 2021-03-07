package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.mongo.UserLocationVo;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service
public class UserLocationApiImpl implements UserLocationApi{

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 上传地址信息服务方法
     * x表示当地的纬bai度，y表示当地的经du度
     * @param latitude 纬度 x
     * @param longitude 经度 y
     * @param addrStr 位置描叙
     */
    @Override
    public void addLocation(Double latitude, Double longitude, String addrStr,Long userId) {
        //根据用户id查询地址位置记录是否存在
        Query query = new Query(Criteria.where("userId").is(userId));
        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
        long time = System.currentTimeMillis();
        if (userLocation != null) {
            //2.如果存在，则更新地址位置
            //Query query, Update update, Class<?> entityClass
            Update update = new Update();
            update.set("location",new GeoJsonPoint(latitude,longitude));
            update.set("address",addrStr);//位置
            update.set("updated",time);
            update.set("lastUpdated",time);
            mongoTemplate.updateFirst(query,update,UserLocation.class);
        }else {
            //3.如果不存在，则保存地址位置
            UserLocation ul = new UserLocation();
            ul.setUserId(userId);//当前用户id
            ul.setLocation(new GeoJsonPoint(latitude,longitude));//经纬度
            ul.setAddress(addrStr);//位置
            ul.setUpdated(time);
            ul.setLastUpdated(time);
            ul.setCreated(time);
            mongoTemplate.save(ul);
        }
    }

    /**
     * 根据搜索距离和当前用户id 搜索附近用户信息
     * withinSphere:地理位置搜索
     * @param userId
     * @param distance
     * @return
     */
    @Override
    public List<UserLocationVo> searchNearBy(Long userId, long distance) {

        //1.根据当前用户id 获取当前用户位置
        Query query = new Query(Criteria.where("userId").is(userId));
        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
        //2.根据当前用户位置 与 距离  搜索附近用户list
        GeoJsonPoint location = userLocation.getLocation();//当前用户位置
        //创建Circle
        //条件 location字段 ： 参数1：当前用户位置   参数2：距离5000米 /1000
        Circle circle = new Circle(location,new Distance(distance/1000,Metrics.KILOMETERS));
        Query locationQuery = new Query(Criteria.where("location").withinSphere(circle));
        List<UserLocation> userLocationList = mongoTemplate.find(locationQuery, UserLocation.class);
        //3.将List<UserLocation>转换为 List<UserLocationVo>
        List<UserLocationVo>  userLocationVoList  = UserLocationVo.formatToList(userLocationList);
        return userLocationVoList;
    }
}
