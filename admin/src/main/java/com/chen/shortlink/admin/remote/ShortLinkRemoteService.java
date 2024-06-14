package com.chen.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.chen.shortlink.admin.remote.dto.req.*;
import com.chen.shortlink.admin.remote.dto.resp.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 短链接中台远程调用服务
 */
@Component
@FeignClient(name = "short-link-project")
public interface ShortLinkRemoteService {


    /**
     * 添加短链接
     * @param shortLinkAddReqDTO
     * @return
     */
    @PostMapping("api/short-link/v1/create")
    Result<ShortLinkAddRespDTO> addShortLink(@RequestBody ShortLinkAddReqDTO shortLinkAddReqDTO);

    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    @GetMapping("api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@SpringQueryMap ShortLinkPageReqDTO shortLinkPageReqDTO);

    /**
     * 统计分组短链接数量
     */
    @GetMapping("api/short-link/v1/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam);




    /**
     * 修改短链接
     * @param shortLinkAddReqDTO
     */
    @PostMapping("api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkAddReqDTO);



    /**
     * 根据 URL 获取title
     * @param url
     */
    @GetMapping("api/short-link/v1/title")
    Result getTitleByUrl(@RequestParam("url") String url);


    /**
     * 短链接添加到回收站
     * @param recycleBinSaveReqDTO
     */
    @PostMapping("api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO);


    /**
     * 分页查询回收站短链接
     */
    @GetMapping("api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@SpringQueryMap ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO);


    /**
     * 移除短链接
     */
    @PostMapping("api/short-link/v1/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO recycleBinRemoveReqDTO);


    /**
     * 恢复短链接
     */
    @PostMapping("api/short-link/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO recycleBinRecoverReqDTO);



    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("api/short-link/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@SpringQueryMap ShortLinkStatsReqDTO shortLinkStatsReqDTO);


    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("api/short-link/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@SpringQueryMap ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);


    /**
     * 统计单个短链接访问记录
     */
    @GetMapping("api/short-link/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@SpringQueryMap ShortLinkStatsAccessRecordReqDTO shortLinkStatsAccessRecordReqDTO);


    /**
     * 统计分组短链接访问记录
     */
    @GetMapping("api/short-link/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@SpringQueryMap ShortLinkGroupStatsAccessRecordReqDTO shortLinkGroupStatsAccessRecordReqDTO);


    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchAddShortLink(@RequestBody ShortLinkBatchCreateReqDTO shortLinkBatchCreateReqDTO);


}
