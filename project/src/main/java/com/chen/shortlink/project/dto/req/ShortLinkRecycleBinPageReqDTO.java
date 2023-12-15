package com.chen.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import lombok.Data;

import java.util.List;

/**
 * 短链接回收站分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDo> {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
