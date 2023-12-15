package com.chen.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dao.mapper.ShortLinkMapper;
import com.chen.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.chen.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.chen.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chen.shortlink.project.service.RecycleBinService;
import com.chen.shortlink.project.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO) {
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                .in(ShortLinkDo::getGid, shortLinkRecycleBinPageReqDTO.getGidList())
                .eq(ShortLinkDo::getEnableStatus, 1)
                .eq(ShortLinkDo::getDelFlag, 0);
        IPage<ShortLinkDo> resultPage = baseMapper.selectPage(shortLinkRecycleBinPageReqDTO, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.convert(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO) {
        LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, recycleBinRecoverReqDTO.getGid())
                .eq(ShortLinkDo::getFullShortUrl, recycleBinRecoverReqDTO.getFullShortUrl())
                .eq(ShortLinkDo::getEnableStatus, 1);
        ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                .enableStatus(0)
                .build();
        baseMapper.update(shortLinkDo,updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, recycleBinRecoverReqDTO.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO) {
        LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, recycleBinRemoveReqDTO.getGid())
                .eq(ShortLinkDo::getFullShortUrl, recycleBinRemoveReqDTO.getFullShortUrl())
                .eq(ShortLinkDo::getEnableStatus, 1)
                .eq(ShortLinkDo::getDelFlag, 0)
                .eq(ShortLinkDo::getDelTime,0L);
        ShortLinkDo shortLinkDo = new ShortLinkDo();
        shortLinkDo.setDelFlag(1);
        shortLinkDo.setDelTime(System.currentTimeMillis());
        baseMapper.update(shortLinkDo,updateWrapper);
    }
}
