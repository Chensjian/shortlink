package com.chen.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class GroupSortReqDTO {

    /**
     * 分组标识id
     */
    private String gid;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
