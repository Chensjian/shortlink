package com.chen.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkRemoteService shortLinkRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        return shortLinkRemoteService.oneShortLinkStats(shortLinkStatsReqDTO);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO shortLinkStatsAccessRecordReqDTO) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(shortLinkStatsAccessRecordReqDTO);
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO shortLinkGroupStatsAccessRecordReqDTO) {
        return shortLinkRemoteService.groupShortLinkStatsAccessRecord(shortLinkGroupStatsAccessRecordReqDTO);
    }


}
