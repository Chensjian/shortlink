package com.chen.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 短连接分组实体
 */
@Data
@TableName("t_group")
public class GroupDo {


    @TableId(type = IdType.AUTO)
    /**
    * id
    */
    private Long id;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 创建分组用户名
    */
    private String username;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 修改时间
    */
    private Date updateTime;

    /**
    * 删除标识 0：未删除 1：已删除
    */
    private int delFlag;

}