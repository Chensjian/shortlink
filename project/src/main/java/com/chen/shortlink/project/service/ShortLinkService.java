package com.chen.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDo> {

    ShortLinkAddRespDTO addShortLink(ShortLinkAddReqDTO shortLinkAddReqDTO);
}
