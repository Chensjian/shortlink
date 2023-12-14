package com.chen.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dao.mapper.ShortLinkMapper;
import com.chen.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.chen.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.chen.shortlink.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.chen.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站管理实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDo> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, recycleBinSaveReqDTO.getGid())
                .eq(ShortLinkDo::getFullShortUrl, recycleBinSaveReqDTO.getFullShortUrl())
                .eq(ShortLinkDo::getEnableStatus, 0)
                .eq(ShortLinkDo::getDelFlag, 0);
        ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                .enableStatus(1)
                .build();
        baseMapper.update(shortLinkDo,updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, recycleBinSaveReqDTO.getFullShortUrl()));
    }
}
