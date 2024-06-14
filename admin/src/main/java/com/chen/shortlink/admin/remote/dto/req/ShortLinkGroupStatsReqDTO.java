package com.chen.shortlink.admin.remote.dto.req;

import lombok.Data;

import java.util.Date;

/**
 * 分组短链接请求参数
 */
@Data
public class ShortLinkGroupStatsReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;
}
