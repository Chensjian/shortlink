package com.chen.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.shortlink.project.common.convention.result.Result;
import com.chen.shortlink.project.common.convention.result.Results;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.chen.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(shortLinkStatsReqDTO));
    }



    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO) {
        return Results.success(shortLinkStatsService.groupShortLinkStats(shortLinkGroupStatsReqDTO));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO shortLinkStatsAccessRecordReqDTO) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(shortLinkStatsAccessRecordReqDTO));
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO shortLinkGroupStatsAccessRecordReqDTO) {
        return Results.success(shortLinkStatsService.groupShortLinkStatsAccessRecord(shortLinkGroupStatsAccessRecordReqDTO));
    }
}
