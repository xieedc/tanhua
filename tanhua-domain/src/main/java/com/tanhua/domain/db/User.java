package com.tanhua.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;


/**
 * 用户实体对象
 */
@Data
public class User implements Serializable {
    private Long id;
    private String mobile; //手机号
    @JSONField(serialize = false)
    private String password; //密码，json序列化时忽略
    private Date created;
    private Date updated;
}
