package com.chen.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkPageRespDTO;

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
}
