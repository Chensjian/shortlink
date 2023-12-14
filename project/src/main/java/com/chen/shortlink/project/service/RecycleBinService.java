package com.chen.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dto.req.RecycleBinSaveReqDTO;

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
}
