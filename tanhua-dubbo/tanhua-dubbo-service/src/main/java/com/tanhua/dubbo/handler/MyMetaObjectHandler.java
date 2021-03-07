package com.tanhua.dubbo.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 公共填充类
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入时自动填充 created updated
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        fillFieldValue("created",new Date(),metaObject);
        fillFieldValue("updated",new Date(),metaObject);
    }

    /**
     * 更新时自动填充 updated
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        //如果updated值为空 则 不更新
        Object updated = getFieldValByName("updated", metaObject);
        if(updated != null){
            fillFieldValue("updated",new Date(),metaObject);
        }
    }

    /**
     * 方法抽取
     * @param field
     * @param value
     * @param metaObject
     */
    private void fillFieldValue(String field,Object value,MetaObject metaObject) {
        setFieldValByName(field,value,metaObject);
    }
}
