package com.chen.shortlink.project.controller;

import com.chen.shortlink.project.common.convention.result.Result;
import com.chen.shortlink.project.common.convention.result.Results;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 添加短链接
     * @param shortLinkAddReqDTO 添加短链接参数
     * @return
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkAddRespDTO> addShortLink(@RequestBody ShortLinkAddReqDTO shortLinkAddReqDTO){
        return Results.success(shortLinkService.addShortLink(shortLinkAddReqDTO));
    }

    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    @GetMapping("/api/short-link/v1/page")
    public Result pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        return Results.success(shortLinkService.pageShortLink(shortLinkPageReqDTO));
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     *  修改短链接
     * @param shortLinkAddReqDTO 修改短链接参数
     * @return
     */
    @PostMapping("/api/short-link/v1/update")
    public Result updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkAddReqDTO){
        shortLinkService.updateShortLink(shortLinkAddReqDTO);
        return Results.success();
    }
}
