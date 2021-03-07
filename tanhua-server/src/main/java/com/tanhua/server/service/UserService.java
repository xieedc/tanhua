package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.dubbo.api.mongo.userLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消费者-业务处理层
 * 用户业务处理层
 */
@Service
@Slf4j
public class UserService {
    //调用服务提供者
    @Reference
    private UserApi userApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private JwtUtils jwtUtils;


    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private userLikeApi userLikeApi;

    @Reference
    private FriendApi friendApi;

    /**
     * 根据手机号码 查询 用户对象
     * ResponseEntity(主要包含 状态码 返回内容)
     */
    public ResponseEntity findByMobile(String mobile) {
        User user = userApi.findByMobile(mobile);
        return ResponseEntity.ok(user);
    }

    /**
     *保存用户
     */
    public ResponseEntity saveUser(String mobile, String password) {
        User user = new User();
        user.setMobile(mobile);
        user.setPassword(password);
        userApi.saveUser(user);
        return ResponseEntity.ok(null);
    }


    /**
     * 注册登录-第一步：发送验证码
     */
    public void sendValidateCode(String phone) {
        //1.设置key  VALIDATECODE_131112222111
        String key = redisValidateCodeKeyPrefix+phone;
        //2.根据key从redis获取验证码
        String redisCode = redisTemplate.opsForValue().get(key);
        //3.验证码存在 验证码还未失效
        if(StringUtils.isNotEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.duplicate());
        }
        //4.验证码不存在，生成验证码  一般情况 4或6
        String code = RandomStringUtils.randomNumeric(6);
        code = "123456";
        //为了方便调试 打印日志
        log.debug("发送验证码：手机号{}验证码{}",phone,code);
        //5.调用阿里云短信发送验证码
        if(false) {
            Map<String, String> rsMap = smsTemplate.sendValidateCode(phone, code);
            //6.发送失败，告知重新获取验证码
            if (rsMap != null) {
                throw new TanHuaException(ErrorResult.error());
            }
        }
        //7.发送成功，将验证码保存redis key value 有效期5分钟
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
        log.debug("验证码发送成功了 ");
    }

    /**
     * 注册登录-第一步：验证码校验(登录)
     */
    public Map<String, Object> loginVerification(String phone, String verificationCode) {
        log.info("输入参数：手机号{},验证码{}：",phone,verificationCode);
        Map<String, Object> map = new HashMap();
        map.put("isNew",false);
        //1根据手机号查询redis是存在验证码
        String key = redisValidateCodeKeyPrefix+phone;
        //redis中验证码
        String redisCode = redisTemplate.opsForValue().get(key);
        //对比验证码之前删除redis验证码
        redisTemplate.delete(key);
        //2如果reidis中验证码不存在，说明验证码已经失效
        if(StringUtils.isEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.loginError());
        }
        //3redis验证存在，拿着用户输入的验证码 跟 redis的验证码 对比
        if(!redisCode.equals(verificationCode)){
            //对比验证码错误，说明验证码输入错误
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        //4验证码正确，根据手机号码查询用户表是否存在用户，
        User user = userApi.findByMobile(phone);

        //5用户不存在，自动注册用户
        if(user == null){
            user = new User();
            //设置手机号  密码（默认手机号码 后6位 并加密）
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex(phone.substring(phone.length()-6)));
            Long userId = userApi.saveUser(user);
            //将保持用户后id放到user对象中
            user.setId(userId);
            //后续生成token的时候要使用
            //如果是新用户 isNew设置=true
            map.put("isNew",true);
            huanXinTemplate.register(userId);//新用户注册环信
            log.debug("用户自动注册了手机号{},userId{}",phone,userId);
        }
        //6用户存在
        //7调用Jwtutils生成token
        String token = jwtUtils.createJWT(phone, user.getId());
        //8将token存入redis(明天使用)
        String userStr = JSON.toJSONString(user);
        redisTemplate.opsForValue().set("TOKEN_"+token,userStr,1,TimeUnit.DAYS);
        //9.返回登录结果token  isNew（true:新用户  false:老用户）
        map.put("token",token);
        log.debug("登录成功了手机号码{},token:{}",phone,token);
        return map;

    }


    /**
     * 通过token获取登陆用户信息
     * @param token
     * @return
     */
    public User getUserByToken(String token){
        log.info("token：：：：：：：{}",token);
        String key = "TOKEN_" + token;
        String userJsonStr = redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(userJsonStr)){
            return null;
        }
        // 延长有效期，续期
        redisTemplate.expire(key,1, TimeUnit.DAYS);
        User user = JSON.parseObject(userJsonStr, User.class);
        log.info("续期token成功");
        return user;
    }

    /**
     * 完善个人信息(保存user_info)
     * @param userInfo
     * @param token
     */
    public void loginReginfo(UserInfo userInfo, String token) {
        log.info("完善个人信息：{} ,token{}",userInfo,token);
        //a.根据TOKEN_xxxx获取用户对象
       /* String userStr = redisTemplate.opsForValue().get("TOKEN_" + token);
        //b.如果用户对象不存在 则重新登录
        if(StringUtils.isEmpty(userStr)){
            throw new TanHuaException("登陆超时，请重新登陆");
        }
        //将userStr转为User对象
        User user = JSON.parseObject(userStr, User.class);
        Long userId = user.getId();*/
        Long userId = UserHolder.getUserId();
        //c.如果存在则调用服务提供者保持用户信息
        userInfo.setId(userId);
        userInfoApi.save(userInfo);
        log.info("保存个人信息成功");
    }
    /**
     * 上传头像
     */
    public void loginReginfoHead(MultipartFile headPhoto, String token) {
        log.info("头像上传{},token{}",headPhoto.getOriginalFilename(),token);
        try {
            //a.根据TOKEN_xxxx获取用户对象
            /*String userStr = redisTemplate.opsForValue().get("TOKEN_" + token);
            //b.如果用户对象不存在 则重新登录
            if(StringUtils.isEmpty(userStr)){
                throw new TanHuaException("登陆超时，请重新登陆");
            }
            User user = JSON.parseObject(userStr, User.class);
            Long userId = user.getId();*/
            Long userId = UserHolder.getUserId();

            //c.如果存在则调用人脸识别 失败 返回“没有检测到人脸 重新上传”
            boolean detect = faceTemplate.detect(headPhoto.getBytes());
            if(!detect){
                throw new TanHuaException("没有检测到人脸，请重新上传");
            }
            log.info("人脸识别成功了....");
            //b.如果识别成功，调用上图图片组件 失败返回 上传失败
            String filename = headPhoto.getOriginalFilename();
            String avatar = ossTemplate.upload(filename, headPhoto.getInputStream());
            log.info("图片上传成功了....");
            //f.上传图片成功，调用服务提供者更新头像 （根据用户id更新tb_user_info 中头像字段）

            UserInfo userInfo  = new UserInfo();//更新头像
            userInfo.setAvatar(avatar);//头像
            userInfo.setId(userId);
            userInfoApi.update(userInfo);
            log.info("头像完善成功了....");
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("上传头像失败",e);
            throw new TanHuaException("上传头像失败，请稍后重试");
        }

    }


    /**
     * 互相喜欢 喜欢 粉丝统计
     * @return
     */
    public CountsVo counts() {
        CountsVo countsVo = new CountsVo();
        Long userId = UserHolder.getUserId();
        //1.互相喜欢 数量
        Long likeEachOther = userLikeApi.countLikeEachOther(userId);
        //2.喜欢 数量
        Long oneSideLike = userLikeApi.countOneSideLike(userId);
        //3.粉丝统计数量
        Long fens = userLikeApi.countFens(userId);
        countsVo.setFanCount(fens);
        countsVo.setLoveCount(oneSideLike);
        countsVo.setEachLoveCount(likeEachOther);
        return countsVo;
    }


    /**
     * 互相喜欢、喜欢、粉丝、谁看过我分页列表查询
     * @param type
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<FriendVo> queryUserLikeList(int type, int page, int pagesize) {
        //1.根据不同的type 查询不同的列表数据PageResult<RecommendUser>
        PageResult pageResult = new PageResult<>();
        Long userId = UserHolder.getUserId();
        //1 互相关注  //2 我关注  //3 粉丝 //4 谁看过我
        switch (type){
            case 1:
                pageResult = userLikeApi.findPageLikeEachOther(userId,page,pagesize);
                break;
            case 2:
                pageResult = userLikeApi.findPageOneSideLike(userId,page,pagesize);
                break;
            case 3:
                pageResult = userLikeApi.findPageFens(userId,page,pagesize);
                break;
            case 4:
                pageResult = userLikeApi.findPageMyVisitors(userId,page,pagesize);
                break;
            default: break;
        }
        //2.根据列表中用户id查询用户信息
        List<RecommendUser> recommendUserList = pageResult.getItems();
        List<FriendVo> friendVoList = new ArrayList<>();
        if(recommendUserList != null && recommendUserList.size()>0){
            for (RecommendUser recommendUser : recommendUserList) {
                FriendVo friendVo = new FriendVo();
                UserInfo userInfo = userInfoApi.findByUserId(recommendUser.getUserId());//查询别人的用户id
                BeanUtils.copyProperties(userInfo,friendVo);
                //匹配度或缘分值
                if(recommendUser.getScore() == null){

                }
                friendVo.setMatchRate(recommendUser.getScore().intValue());
                friendVoList.add(friendVo);
            }
        }
        pageResult.setItems(friendVoList);
        //3.返回VO
        return pageResult;
    }

    /**
     * 粉丝-喜欢
     * @param fansUserId
     */
    public void fansLike(Long fansUserId) {

        Long userId = UserHolder.getUserId();
        //1.根据当前用户id 和 粉丝的用户id 删除UserLike中 粉丝喜欢记录
        userLikeApi.delete(userId,fansUserId);
        //2.根据当前用户id 和 粉丝的用户id 往tanhua_users好友表 插入两条数据
        friendApi.add(userId,fansUserId);
        //3.根据当前用户id 和 粉丝的用户id 调用环信通信makeFriends
        huanXinTemplate.makeFriends(userId,fansUserId);

    }
}
