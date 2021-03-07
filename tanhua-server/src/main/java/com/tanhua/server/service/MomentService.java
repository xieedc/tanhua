package com.tanhua.server.service;

import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.visitorApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;

import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 圈子-业务逻辑处理层
 */
@Service
public class MomentService {

    @Autowired
    private OssTemplate ossTemplate;

    @Reference
    private PublishApi publishApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private CommentApi commentApi;

    @Reference
    private visitorApi visitorApi;
    /**
     * 圈子发布动态
     * @param publishVo
     * @param imageContent
     * @throws IOException
     */
    public void postMoment(PublishVo publishVo, MultipartFile[] imageContent) throws IOException {
        //对页面图片参数处理
        ArrayList<String> medias = new ArrayList<>();
        if (imageContent != null){
            for (MultipartFile multipartFile : imageContent) {
                String filename = multipartFile.getOriginalFilename();
                String upload = ossTemplate.upload(filename, multipartFile.getInputStream());
                medias.add(upload);
            }
        }
        //调用服务提供者方法 发布动态
        publishVo.setMedias(medias);
        publishVo.setUserId(UserHolder.getUserId());
        publishApi.add(publishVo);

    }

    /**
     *     圈子-查询好友动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> queryFriendPublishList(int page, int pagesize) {
        //定义返回vo
        PageResult<MomentVo> vopageResult = new PageResult<>();
        //获取当前登录用户id
        Long userId = UserHolder.getUserId();
        //1调用服务提供者方法 分页查询好友动态 (时间线表quanzi_time_line_1)
        PageResult<Publish> pageResult = publishApi.queryFriendPublishList(page,pagesize,userId);
        List<Publish> items = pageResult.getItems();
        //将List<Publish> 转为List<MomentVo>
        ArrayList<MomentVo> momentVos = new ArrayList<>();
        //2根据userId  到 userInfo表中查询用户信息
        if (items != null && items.size()>0){
            for (Publish  publish : items) {
                MomentVo momentVo = new MomentVo();
                //某个用户发布的动态
                //根据发布用户的id查询  用户信息表
                UserInfo userInfo = userInfoApi.findByUserId(publish.getUserId());
                BeanUtils.copyProperties(userInfo,momentVo);
                //设置用户id
                momentVo.setUserId(userId);
                //设置标签
                if (StringUtils.isNotEmpty(userInfo.getTags())){
                    momentVo.setTags(userInfo.getTags().split(","));
                }
                //动态数据设置
                BeanUtils.copyProperties(publish,momentVo);
                //发布id
                momentVo.setId(publish.getId().toHexString());
                //将图片动态列表  将List<String> 转为 String []
                momentVo.setImageContent(publish.getMedias().toArray(new String[] {}));
                momentVo.setDistance("距离50米");
                //设置动态发布时间
                momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                String key = "publish_like_" + userId + "_" + publish.getId().toHexString();
                if (redisTemplate.hasKey(key)) {
                    momentVo.setHasLiked(1);
                }else {
                    momentVo.setHasLiked(0);
                }

                String key2 = "publish_love_" + userId+"_" + publish.getId().toHexString();
                if(redisTemplate.hasKey(key2)){
                    momentVo.setHasLoved(1);  //是否喜欢  0：未点 1:点赞
                }
                else
                {
                    momentVo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
                }
                momentVos.add(momentVo);
            }
        }
        //返回vo
        BeanUtils.copyProperties(pageResult,vopageResult);
        vopageResult.setItems(momentVos);//将转换后的数据放到voPageResult
        return vopageResult;
    }

    /**
     * 圈子-推荐动态
     */
    public PageResult<MomentVo> queryRecommendPublishList(int page, int pagesize) {
        //定义返回VO
        PageResult<MomentVo> voPageResult = new PageResult<>();
        //获取当前登录用户id
        Long userId = UserHolder.getUserId();
        //1调用服务提供者方法 分页查询好友动态 (时间线表quanzi_time_line_1)
        PageResult<Publish>  pageResult = publishApi.queryRecommendPublishList(page,pagesize,userId);
        List<Publish> items = pageResult.getItems();
        //将List<Publish> 转为List<MomentVo>
        List<MomentVo> momentVos = new ArrayList<>();
        //2根据userId  到 userInfo表中查询用户信息
        if(items != null && items.size()>0){
            for (Publish publish : items) {
                MomentVo momentVo = new MomentVo();
                //某个用户发布的动态
                //根据发布用户的id查询  用户信息表
                UserInfo userInfo = userInfoApi.findByUserId(publish.getUserId());
                BeanUtils.copyProperties(userInfo,momentVo);
                //设置用户id
                momentVo.setUserId(userId);
                //设置标签
                if(StringUtils.isNotEmpty(userInfo.getTags())) {
                    momentVo.setTags(userInfo.getTags().split(","));
                }
                //动态数据设置
                BeanUtils.copyProperties(publish,momentVo);
                //发布id
                momentVo.setId(publish.getId().toHexString());
                //将图片动态列表  将List<String> 转为 String []
                momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
                momentVo.setDistance("距离50米");
                //设置动态发布时间
                momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                String key = "publish_like_" + userId + "_" + publish.getId().toHexString();
                if (redisTemplate.hasKey(key)) {
                    momentVo.setHasLiked(1);
                } else {
                    momentVo.setHasLiked(0);
                }
                String key2 = "publish_love_" + userId+"_" + publish.getId().toHexString();
                if(redisTemplate.hasKey(key2)){
                    momentVo.setHasLoved(1);  //是否喜欢  0：未点 1:点赞
                }
                else
                {
                    momentVo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
                }
                momentVo.setHasLoved(0);
                momentVos.add(momentVo);
            }
        }
        //3返回VO
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(momentVos);//将转换后的数据放到voPageResult
        return voPageResult;
    }

    /**
     * 我的动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    public PageResult<MomentVo> queryMyAlbum(int page, int pagesize, Long userId) {
        //定义返回vo
        PageResult<MomentVo> voPageResult = new PageResult<>();
        //1调用服务提供者方法 分页查询好友动态 (时间线表quanzi_time_line_1)
        PageResult<Publish> pageResult = publishApi.queryMyAlbum(page,pagesize,userId);
        List<Publish> items = pageResult.getItems();
        //将List<Publish> 转为List<MomentVo>
        ArrayList<MomentVo> momentVos = new ArrayList<>();
        //根据userId 到 userInfo表中查询用户信息
        if (items != null && items.size() > 0) {
            for (Publish publish : items) {
                MomentVo momentVo = new MomentVo();
                //某个用户发布的动态
                //根据发布用户的id查询  用户信息表
                UserInfo userInfo = userInfoApi.findByUserId(publish.getUserId());
                BeanUtils.copyProperties(userInfo,momentVo);
                //设置用户id
                momentVo.setUserId(userId);
                //设置标签
                if (StringUtils.isNotEmpty(userInfo.getTags())){
                    momentVo.setTags(userInfo.getTags().split(","));
                }
                //动态数据设置
                BeanUtils.copyProperties(publish,momentVo);
                //发布id
                momentVo.setId(publish.getId().toHexString());
                //将图片动态列表 将List<String> 转为 String []
                momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
                momentVo.setDistance("距离50米");
                //设置动态发布时间
                momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                String key = "publish_like_" + userId + "_" + publish.getId().toHexString();
                if (redisTemplate.hasKey(key)) {
                    momentVo.setHasLiked(1);
                }else {
                    momentVo.setHasLiked(0);
                }
                String key2 = "publish_love_" + userId+"_" + publish.getId().toHexString();
                if(redisTemplate.hasKey(key2)){
                    momentVo.setHasLoved(1);  //是否喜欢  0：未点 1:点赞
                }
                else
                {
                    momentVo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
                }

                momentVos.add(momentVo);
            }
        }
        //返回vo
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(momentVos);//将转换后的数据放到voPageResult
        return voPageResult;
    }

    /**
     * 动态-点赞
     * @param publishId
     * @return
     */
    public long like(String publishId) {
        Long userId = UserHolder.getUserId();
        //创建Comment
        Comment comment = new Comment();
        //调用服务点赞方法+1
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(1); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前登录用户id
        long total = (commentApi.sava(comment));
        //b.将点赞记录写入redis (动态查询的时候 看redis记录是否存在，存在则选中 不存在则不选中)
        String key = "publish_like_" + userId + "_" + publishId;
        redisTemplate.opsForValue().set(key,"1");
        return total;
    }

    /**
     * 动态-取消点赞
     * @param publishId
     * @return
     */
    public long dislike(String publishId) {
        Long userId = UserHolder.getUserId();
        //a. 调用服务点赞方法-1
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(1);  //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前登录用户id
        long total = commentApi.remove(comment);
        //b.将点赞记录从redis删除
        String key = "publish_like_" + userId + "_" + publishId;
        redisTemplate.delete(key);
        return total;
    }

    /**
     * 动态-喜欢
     * @param publishId
     * @return
     */
    public long love(String publishId) {
        Long userId = UserHolder.getUserId();
        //调用服务点赞方法+1
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(3);  //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前登录用户id
        long total = commentApi.sava(comment);
        //b.将点赞记录写入redis (动态查询的时候 看redis记录是否存在，存在则选中 不存在则不选中)
        String key = "publish_love" + userId + "_" +publishId;
        redisTemplate.opsForValue().set(key,"1");
        return total;
    }

    /**
     * 动态-取消喜欢
     * @param publishId
     * @return
     */
    public long unlove(String publishId) {
        Long userId = UserHolder.getUserId();
        //a. 调用服务点赞方法-1
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(3);  //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前登录用户id
        long total = commentApi.remove(comment);
        //将点赞数记录从redis删除
        String key = "publish_love_" + userId + "_" + publishId;
        redisTemplate.delete(key);
        return total;
    }


    /**
     * 单条动态查询
     * @param publishId
     * @return
     */
    public MomentVo queryById(String publishId) {
        //定义返回vo
        MomentVo momentVo = new MomentVo();
        //获取当前登录用户id
        Long userId = UserHolder.getUserId();
        //1调用服务提供者方法 查询单条动态查询
        Publish publish = publishApi.findById(publishId);
        //某个用户发布的动态
        //根据发布用户的id查询  用户信息表
        UserInfo userInfo = userInfoApi.findByUserId(publish.getUserId());
        BeanUtils.copyProperties(userInfo,momentVo);
        //设置用户id
        momentVo.setUserId(userId);
        //设置标签
        if (StringUtils.isNotEmpty(userInfo.getTags())){
            momentVo.setTags(userInfo.getTags().split(","));
        }
        //动态数据设置
        BeanUtils.copyProperties(publish,momentVo);
        //发布id
        momentVo.setId(publish.getId().toHexString());
        //将图片动态列表  将List<String> 转为 String []
        momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
        momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
        return momentVo;
    }

    /**
     * 谁看过我
     * @return
     */
    public List<VisitorVo> queryVisitors() {
        //userId
        Long userId = UserHolder.getUserId();
        //定义返回VO
        List<VisitorVo> visitorVoList = new ArrayList<>();
        String key = "visitors_time_"+userId;
        String time =(String)redisTemplate.opsForValue().get(key);
        //查看redis上次访问时间是否存在，如果存在则根据date>上次访问时间+userId 查询前5条
        List<Visitor> visitorList = new ArrayList<>();
        if(StringUtils.isNotEmpty(time)){
            visitorList =visitorApi.queryVisitors(time,userId);
        }
        else
        {
            //上次访问时间不存在，直接当前用户id查询前5条
            visitorList = visitorApi.queryVisitors(userId);
        }
        //根据访客userId 到userInfo表查询访客信息
        if(visitorList != null && visitorList.size()>0){
            for (Visitor visitor : visitorList) {
                VisitorVo visitorVo = new VisitorVo();
                Long visitorUserId = visitor.getVisitorUserId();//访客用户id
                UserInfo userInfo = userInfoApi.findByUserId(visitorUserId);
                visitorVo.setId(visitorUserId);//访客的用户id
                visitorVo.setNickname(userInfo.getNickname());//昵称
                visitorVo.setAvatar(userInfo.getAvatar());//头像
                visitorVoList.add(visitorVo);
            }
        }
        redisTemplate.opsForValue().set(key,System.currentTimeMillis()+"");
        //返回VO
        return visitorVoList;

    }
}
