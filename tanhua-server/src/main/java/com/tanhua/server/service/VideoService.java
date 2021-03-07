package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小视频管理业务处理层
 */
@Service
@Slf4j
public class VideoService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient Client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Reference
    private VideoApi videoApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 上传小视频
     * @param videoThumbnail
     * @param videoFile
     * @throws IOException
     */
    @CacheEvict(value = "videoList",allEntries = true) //清空redis缓存
    public void post(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        log.info("*************上传小视频********");
        //1.将封面上传阿里云oss
        String picUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        //获取视频 原始文件名称
        String originalFilename = videoFile.getOriginalFilename();
        //文件后缀
        String suffix = originalFilename.substring(originalFilename.indexOf("."));
        //2.将小视频上传fastDFS
        //InputStream inputStream, long fileSize, String fileExtName, Set<MetaData> metaDataSet
        StorePath storePath = Client.uploadFile(videoFile.getInputStream(),videoFile.getSize(),suffix,null);
        //完整的视频访问路径（fastDFS）
        String videoUrl = fdfsWebServer.getWebServerUrl()+storePath.getFullPath();
        //3.调用服务保存小视频记录
        Video video = new Video();
        video.setUserId(UserHolder.getUserId());//当前发布视频的用户id
        video.setText("百鸟朝凤");//默认文字
        video.setPicUrl(picUrl);//图片封面
        video.setVideoUrl(videoUrl);//视频地址
        videoApi.save(video);
    }


    /**
     * 小视频列表查询
     * @param page
     * @param pagesize
     * @return
     */
    @Cacheable(value = "videoList",key="#page + '_'+#pagesize") //将数据保存redis 1_10 2_10 3_10
    public PageResult<VideoVo> findPage(int page, int pagesize) {
        log.info("*************小视频列表查询********");
        //构造返回VO
        PageResult<VideoVo> videoVoPageResult = new PageResult<>();

        //1.service调用服务分页查询视频列表数据
        PageResult<Video> videoPageResult  = videoApi.findPage(page,pagesize);
        List<Video> videoList = videoPageResult.getItems();//小视频分页数据
        List<VideoVo> videoVoList = new ArrayList<>();
        //2根据视频发布用户id查询用户信息表
        if(videoList!= null && videoList.size()>0){
            for (Video video : videoList) {
                VideoVo videoVo = new VideoVo();
                //将video相同的属性复制到VO
                BeanUtils.copyProperties(video,videoVo);
                //根据小视频发布用户id 查询用户信息
                UserInfo userInfo = userInfoApi.findByUserId(video.getUserId());
                BeanUtils.copyProperties(userInfo,videoVo);
                //设置不同id字段
                videoVo.setId(video.getId().toHexString());//主键id
                videoVo.setSignature(video.getText());//签名
                videoVo.setCover(video.getPicUrl());//封面
                //默认值
                videoVo.setHasLiked(0);//是否点赞
                String key = "video_follow_" + UserHolder.getUserId()+ "_" + video.getUserId();
                if(redisTemplate.hasKey(key)){
                    videoVo.setHasFocus(1);//是否关注
                }
                else {
                    videoVo.setHasFocus(0);//是否关注
                }
                videoVoList.add(videoVo);
            }
        }

        //3.返回VO
        BeanUtils.copyProperties(videoPageResult,videoVoPageResult);
        videoVoPageResult.setItems(videoVoList);
        return videoVoPageResult;
    }

    /**
     * 关注用户
     * @param followUserId
     */
    public void followUser(long followUserId) {
        //userId: 需要被关注的用户id
        Long userId = UserHolder.getUserId();//当前登录用户id
        //1.将当前用户id和关注用户id保存关注表
        videoApi.followUser(userId,followUserId);

        //2.将当前用户id和关注用户id保存关注表中
        String key = "video_follow_" + userId + "_" + followUserId;
        redisTemplate.opsForValue().set(key,"1");

    }

    /**
     * 取消关注用户
     * @param followUserId
     */
    public void unFollowUser(long followUserId) {
        //userId: 需要被关注的用户id
        Long userId = UserHolder.getUserId();//当前登录用户id
        //1.将当前用户id和关注用户id保存关注表
        videoApi.unFollowUser(userId,followUserId);

        //2.将当前用户id和关注用户id保存关注表中
        String key = "video_follow_" + userId + "_" + followUserId;
        redisTemplate.delete(key);
    }
}
