package com.chen.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 添加短链接分组实体
 */
@Data
public class GroupAddReqDTO {

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
