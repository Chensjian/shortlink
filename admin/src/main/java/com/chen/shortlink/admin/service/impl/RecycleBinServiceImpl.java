package com.chen.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.admin.common.biz.user.UserContext;
import com.chen.shortlink.admin.common.convention.exception.ClientException;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.dao.entity.GroupDo;
import com.chen.shortlink.admin.dao.mapper.GroupMapper;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.chen.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * URL 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;
    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO) {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getDelFlag, 0);
        List<GroupDo> groupDoList = groupMapper.selectList(queryWrapper);
        if(CollectionUtil.isEmpty(groupDoList)){
            throw new ClientException("用户无分组信息");
        }
        shortLinkRecycleBinPageReqDTO.setGidList(groupDoList.stream().map(GroupDo::getGid).collect(Collectors.toList()));
        return shortLinkRemoteService.pageShortLink(shortLinkRecycleBinPageReqDTO);
    }
}
