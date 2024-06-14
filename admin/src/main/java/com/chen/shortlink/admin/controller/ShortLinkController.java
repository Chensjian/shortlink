package com.chen.shortlink.admin.controller;

import com.chen.shortlink.admin.annotation.MyLog;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.common.convention.result.Results;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.admin.util.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {


    private final ShortLinkRemoteService shortLinkRemoteService;
//    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};

    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        return shortLinkRemoteService.pageShortLink(shortLinkPageReqDTO);
    }

    /**
     * 添加短链接
     * @param shortLinkAddReqDTO 添加短链接参数
     * @return
     */
    @MyLog(title = "短链接模块",content = "添加短链接")
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkAddRespDTO> addShortLink(@RequestBody ShortLinkAddReqDTO shortLinkAddReqDTO){
        return shortLinkRemoteService.addShortLink(shortLinkAddReqDTO);
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/admin/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return shortLinkRemoteService.listGroupShortLinkCount(requestParam);
    }

    /**
     *  修改短链接
     * @param shortLinkAddReqDTO 修改短链接参数
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/update")
    public Result updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkAddReqDTO){
        shortLinkRemoteService.updateShortLink(shortLinkAddReqDTO);
        return Results.success();
    }

    /**
     * 批量添加短链接
     */
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchAddShortLink(@RequestBody ShortLinkBatchCreateReqDTO shortLinkBatchCreateReqDTO, HttpServletResponse response){
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkRemoteService.batchAddShortLink(shortLinkBatchCreateReqDTO);
        if(shortLinkBatchCreateRespDTOResult.isSuccess()){
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }
}
