package com.chen.shortlink.admin.controller;

import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.common.convention.result.Results;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.req.ShortLinkPageReqDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortLinkController {


    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){

        };
        return shortLinkRemoteService.pageShortLink(shortLinkPageReqDTO);
    }
}
