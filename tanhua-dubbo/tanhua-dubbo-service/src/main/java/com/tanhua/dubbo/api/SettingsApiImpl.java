package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.dao.SettingsDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class SettingsApiImpl implements SettingsApi{
    @Autowired
    private SettingsDao settingsDao;

    @Override
    public Settings findByUserId(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return settingsDao.selectOne(queryWrapper);
    }

    @Override
    public void update(Settings settings) {
        settingsDao.updateById(settings);
    }

    @Override
    public void save(Settings settings) {
        settingsDao.insert(settings);
    }
}
