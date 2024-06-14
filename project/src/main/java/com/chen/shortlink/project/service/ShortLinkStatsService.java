package com.chen.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO shortLinkStatsAccessRecordReqDTO);

    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO shortLinkGroupStatsAccessRecordReqDTO);
}
