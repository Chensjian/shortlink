package com.chen.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class GroupUpdateReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
