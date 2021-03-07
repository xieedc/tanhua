package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Question;

public interface QuestionApi {
    /**
     * 根据用户id查询问题数据
     * @param userId
     * @return
     */
    Question findByUserId(Long userId);

    /**
     * //存在 则更新问题表
     * @param question
     */
    void update(Question question);


    /**
     *  //c不存在 则保存问题表
     * @param question
     */
    void save(Question question);
}
