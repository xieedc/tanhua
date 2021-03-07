package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Settings;

public interface SettingsApi {

    /**
     * 根据用户id查询通知设置数据
     * @param userId
     * @return
     */
    Settings findByUserId(Long userId);

    /**
     * 则更新通知表
     * @param settings
     */
    void update(Settings settings);

    /**
     * 则保存通知表
     * @param settings
     */
    void save(Settings settings);
}
