package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息处理业务层
 */
@Service
public class IMService {

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private CommentApi commentApi;


    /**
     * 回复陌生人问题
     * @param paramMap
     */
    public void replyStrangerQuestions(Map<String, Object> paramMap) {
        String reply = (String) paramMap.get("reply");
        Long userId2 = Long.parseLong(paramMap.get("userId").toString());
        Long userId = UserHolder.getUserId();
        //1根据当前登录用户id查询用户信息
        UserInfo userInfo = userInfoApi.findByUserId(userId);
        //2根据接收消息用户id 查询问题表 得到问题txt
        Question question = questionApi.findByUserId(userId2);
        //3构造消息,调用环信sendMsg
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId.toString());
        map.put("nickname",userInfo.getNickname());
        map.put("strangerQuestion",question == null ? "今晚约吗" : question.getTxt());
        map.put("reply",reply);
        //给佳人发送消息
        String toUserId = userId2.toString();
        //消息内容
        String msg = JSON.toJSONString(map);
        huanXinTemplate.sendMsg(toUserId,msg);
    }

    /**
     * 联系人添加
     * @param paramMap
     */
    public void addContacts(Map<String, Long> paramMap) {
        //添加为好友
        Long friendId = paramMap.get("userId");
        Long userId = UserHolder.getUserId();
        //往tanhua_users表中保存两条记录
        friendApi.add(userId,friendId);
        //调用环信通信makeFriends方法成为好友
        huanXinTemplate.makeFriends(userId,friendId);
    }

    /**
     * 联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    public PageResult<ContactVo> queryContacts(Integer page, Integer pagesize, String keyword) {
        //1.根据page pagesize keyword 查询联系人列表
        Long userId = UserHolder.getUserId();
        PageResult pageResult = friendApi.queryContacts(page,pagesize,keyword,userId);
        List<Friend> friendList = pageResult.getItems();
        List<ContactVo> contactVoList = new ArrayList<>();
        //2.根据好友ids循环查询userInfo数据
        if (friendList != null && friendList.size()>0){
            for (Friend friend : friendList) {
                ContactVo contactVo = new ContactVo();
                //好友id
                Long friendId = friend.getFriendId();
                UserInfo userInfo = userInfoApi.findByUserId(friendId);
                BeanUtils.copyProperties(userInfo,contactVo);
                //单独设置好友id
                contactVo.setUserId(friendId.toString());
                contactVoList.add(contactVo);
            }
        }
        pageResult.setItems(contactVoList);
        return pageResult;
    }

    /**
     *
     * 点赞 评论  喜欢 列表查询
     * 评论类型，1-点赞，2-评论，3-喜欢
     * @param page
     * @param pagesize
     * @param type
     * @return
     */
    public PageResult<MessageVo> messageCommentList(Integer page, Integer pagesize, int type) {
        //获取当前登录用户id
        Long userId = UserHolder.getUserId();
        //根据点赞、评论、喜欢的用户id (当前登录用户id) 以及   type=1 2 3  得到 PageResult<Comment>
        PageResult pageResult = commentApi.messageCommentList(page,pagesize,type,userId);
        List<Comment> commentList = pageResult.getItems();
        //返回VO
        List<MessageVo> messageVoList = new ArrayList<>();
        //根据根据List<UserIds>再查询用户信息表tb_userInfo（mysql）
        if(commentList != null && commentList.size()>0){
            for (Comment comment : commentList) {
                MessageVo messageVo = new MessageVo();
                UserInfo userInfo = userInfoApi.findByUserId(comment.getUserId());//点赞用户id
                messageVo.setId(userInfo.getId().toString());//点赞 评论  喜欢的用户id
                messageVo.setAvatar(userInfo.getAvatar());//头像
                messageVo.setNickname(userInfo.getNickname());//昵称
                messageVo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(comment.getCreated()));
                messageVoList.add(messageVo);
            }
        }
        //返回VO
        pageResult.setItems(messageVoList);
        return pageResult;
    }
}
