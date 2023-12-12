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
public class GroupDo extends BaseDo {


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
     * 分组名称
     */
    private String name;

    /**
    * 创建分组用户名
    */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;



}