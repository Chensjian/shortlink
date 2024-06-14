package com.chen.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接基础信息响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkBaseInfoRespDTO {
    /**
     * 描述信息
     */
    private String description;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 短链接
     */
    private String fullShortUrl;
}
