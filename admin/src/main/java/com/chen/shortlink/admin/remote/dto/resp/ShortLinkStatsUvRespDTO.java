package com.chen.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接访客监控响应参数
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsUvRespDTO {

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问类型
     */
    private String uvType;

    /**
     * 占比
     */
    private Double ratio;
}
