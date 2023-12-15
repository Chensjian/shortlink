package com.chen.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.common.convention.result.Results;
import com.chen.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.chen.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.chen.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};
    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        shortLinkRemoteService.saveRecycleBin(recycleBinSaveReqDTO);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return recycleBinService.pageRecycleBinShortLink(requestParam);
    }

    /**
     * 移除短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO recycleBinRemoveReqDTO) {
        shortLinkRemoteService.removeRecycleBin(recycleBinRemoveReqDTO);
        return Results.success();
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO recycleBinRecoverReqDTO) {
        shortLinkRemoteService.recoverRecycleBin(recycleBinRecoverReqDTO);
        return Results.success();
    }


}
