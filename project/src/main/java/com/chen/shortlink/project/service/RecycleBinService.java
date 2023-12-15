package com.chen.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.chen.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.chen.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLinkDo> {

    /**
     * 保存回收站
     *
     * @param recycleBinSaveReqDTO 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO);

    /**
     * 分页查询短链接
     *
     * @param shortLinkRecycleBinPageReqDTO 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO);

    /**
     * 恢复短链接
     * @param recycleBinRecoverReqDTO 恢复短链接请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO);

    /**
     * 短链接移除
     * @param recycleBinRemoveReqDTO 短链接移除请求参数
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO);
}
