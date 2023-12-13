package com.chen.shortlink.admin.controller;

import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShortLinkController {


    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};

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
}
