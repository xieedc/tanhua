package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.dao.BlackListDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 黑名单服务实现类
 */
@Service
public class BlackListApiImpl implements BlackListApi{
    @Autowired
    private BlackListDao blackListDao;

    /**
     * 统一将查询黑名单表 以及 用户信息表
     * 多表关联查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<UserInfo> findBlackList(int page, int pagesize, Long userId) {
        PageResult<UserInfo> pageResult = new PageResult<>();
        Page pg = new Page(page,pagesize);
        //查询黑名单表selectPage
        IPage<UserInfo> userInfoIPage = blackListDao.findBlackList(pg,userId);
        pageResult.setItems(userInfoIPage.getRecords());
        pageResult.setCounts(userInfoIPage.getTotal());
        pageResult.setPagesize((long) pagesize);
        pageResult.setPages(userInfoIPage.getPages());
        pageResult.setPage((long)page);
        return pageResult;
    }

    @Override
    public void delBlacklist(String deleteUserId, Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("black_user_id",deleteUserId);
        blackListDao.delete(queryWrapper);
    }
}
