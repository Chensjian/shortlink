package com.chen.shortlink.project.service;

import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO);
}
