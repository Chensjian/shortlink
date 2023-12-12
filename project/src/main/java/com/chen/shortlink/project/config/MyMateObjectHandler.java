package com.chen.shortlink.project.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MyMateObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"createTime",()->new Date(),Date.class);
        this.strictInsertFill(metaObject,"updateTime",()->new Date(),Date.class);
        this.strictInsertFill(metaObject,"delFlag",()->0,Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"updateTime",()->new Date(),Date.class);
    }
}
