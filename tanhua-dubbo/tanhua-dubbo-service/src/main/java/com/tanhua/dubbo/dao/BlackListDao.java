package com.tanhua.dubbo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BlackListDao extends BaseMapper<BlackList> {
    /**
     * 查询黑名单列表
     * @param pg
     * @return
     */
    @Select("select tui.* from tb_user_info tui,tb_black_list tbl where  tbl.black_user_id = tui.id and tbl.user_id = #{userId}")
    IPage<UserInfo> findBlackList(Page pg, @Param("userId") Long userId);
}
