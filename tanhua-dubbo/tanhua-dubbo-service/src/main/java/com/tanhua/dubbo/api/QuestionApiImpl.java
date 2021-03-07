package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.dubbo.dao.QuestionDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class QuestionApiImpl implements QuestionApi{
    @Autowired
    private QuestionDao questionDao;

    @Override
    public Question findByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return questionDao.selectOne(queryWrapper);
    }

    //存在 则更新问题表
    @Override
    public void update(Question question) {
        questionDao.updateById(question);
    }

    //c不存在 则保存问题表
    @Override
    public void save(Question question) {
        questionDao.insert(question);
    }
}
