package com.chen.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDo> {

    /**
     * 添加短链接
     * @param shortLinkAddReqDTO
     * @return
     */
    ShortLinkAddRespDTO addShortLink(ShortLinkAddReqDTO shortLinkAddReqDTO);


    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO);

    /**
     * 查询短链接分组内数量
     * @param requestParam
     * @return
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     *  修改短链接
     * @param shortLinkAddReqDTO 修改短链接参数
     * @return
     */
    void updateShortLink(ShortLinkUpdateReqDTO shortLinkAddReqDTO);

    /**
     * 短链接跳转原始链接
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 批量添加短链接
     */
    ShortLinkBatchCreateRespDTO batchAddShortLink(ShortLinkBatchCreateReqDTO shortLinkBatchCreateReqDTO);

    /**
     * 短链接统计
     */
    void shortLinkStats(String gid, ShortLinkStatsRecordDTO statsRecord);
}
