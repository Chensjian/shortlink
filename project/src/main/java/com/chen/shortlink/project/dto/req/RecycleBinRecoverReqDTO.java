package com.chen.shortlink.project.dto.req;

import lombok.Data;

/**
 * 短链接恢复请求参数
 */
@Data
public class RecycleBinRecoverReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
