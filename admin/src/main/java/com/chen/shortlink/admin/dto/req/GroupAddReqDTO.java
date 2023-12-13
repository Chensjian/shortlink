package com.chen.shortlink.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加短链接分组实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
