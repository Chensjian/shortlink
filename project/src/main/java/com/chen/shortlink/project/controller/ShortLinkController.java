package com.chen.shortlink.project.controller;

import com.chen.shortlink.project.common.convention.result.Result;
import com.chen.shortlink.project.common.convention.result.Results;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 添加短链接
     * @param shortLinkAddReqDTO 添加短链接参数
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkAddRespDTO> addShortLink(@RequestBody ShortLinkAddReqDTO shortLinkAddReqDTO){
        return Results.success(shortLinkService.addShortLink(shortLinkAddReqDTO));
    }
}
