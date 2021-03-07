package com.tanhua.server.service;


import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 通用设置业务层处理
 */
@Service
public class SettingsService {
    @Reference
    private SettingsApi settingsApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private BlackListApi blackListApi;
    /**
     * 查询通用设置
     * @return
     */
    public SettingsVo settings() {
        //定义返回vo
        SettingsVo vo = new SettingsVo();
        //a.获取用户手机号码
        User user = UserHolder.getUser();
        String mobile = user.getMobile();
        Long userId = user.getId();
        vo.setPhone(mobile);
        //b.获取通知设置数据
        Settings settings = settingsApi.findByUserId(userId);
        //新的方式
        if (settings != null){
            BeanUtils.copyProperties(settings,vo);
        }
        //c.获取问题
        Question question = questionApi.findByUserId(userId);
        if (question != null) {
            String txt = question.getTxt();
            if (StringUtils.isNotEmpty(txt)) {
                vo.setStrangerQuestion(txt);
            }else {
                vo.setStrangerQuestion("你长的漂亮吗?");
            }
        }else {
            // 为空 设置默认问题
            vo.setStrangerQuestion("你长的漂亮吗?");
        }
        //d.拼接返回VO（VO跟接口文档返回数据完整一致 ）
        return vo;
    }

    /**
     * 通知设置 - 保存
     */
    public void notifications(boolean likeNotification, boolean pinglunNotification, boolean gonggaoNotification) {
        //a.根据用户id查询通知表 记录是否存在
        Long userId = UserHolder.getUserId();
        Settings settings = settingsApi.findByUserId(userId);
        //存在 则更新通知表
        if (settings != null){
            settings.setLikeNotification(likeNotification);
            settings.setGonggaoNotification(gonggaoNotification);
            settings.setPinglunNotification(pinglunNotification);
            settingsApi.update(settings);
        }
        else
        {
            //c不存在 则保存通知表
            settings = new Settings();
            settings.setLikeNotification(likeNotification);
            settings.setGonggaoNotification(gonggaoNotification);
            settings.setPinglunNotification(pinglunNotification);
            settingsApi.save(settings);
        }
    }

    /**
     * 查询黑名单列表
     */
    public PageResult findBlackList(int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        PageResult<UserInfo> pageResult = blackListApi.findBlackList(page,pagesize,userId);
        return pageResult;
    }

    /**
     * 移除黑名单
     * @param deleteUserId
     */
    public void delBlacklist(String deleteUserId) {
        //1、获取当前用户的userid
        Long userId = UserHolder.getUserId();
        //2、调用api删除黑名单数据
        blackListApi.delBlacklist(deleteUserId,userId);
    }

    /**
     * 设置陌生人问题 - 保存
     */
    public void questions(String content) {
        //a.根据用户id查询问题表 记录是否存在
        Long userId = UserHolder.getUserId();
        Question question = questionApi.findByUserId(userId);
        if (question != null) {
            //存在 则更新问题表
            question.setUserId(userId);
            question.setTxt(content);
            questionApi.update(question);
        }else {
            //c不存在 则保存问题表
            question = new Question();
            question.setUserId(userId);
            question.setTxt(content);
            questionApi.save(question);
        }
    }


}
