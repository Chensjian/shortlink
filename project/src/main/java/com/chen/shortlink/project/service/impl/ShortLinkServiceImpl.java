package com.chen.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.common.convention.exception.ClientException;
import com.chen.shortlink.project.common.enums.VailDateTypeEnum;
import com.chen.shortlink.project.dao.entity.*;
import com.chen.shortlink.project.dao.mapper.*;
import com.chen.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chen.shortlink.project.dto.resp.*;
import com.chen.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.chen.shortlink.project.service.LinkStatsTodayService;
import com.chen.shortlink.project.service.ShortLinkService;
import com.chen.shortlink.project.util.BeanUtil;
import com.chen.shortlink.project.util.HashUtil;
import com.chen.shortlink.project.util.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chen.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.chen.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.chen.shortlink.project.common.enums.ShortLinkErrorEnums.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDo> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkCreateBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOSStatsMapper linkOSStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkStatsTodayService linkStatsTodayService;
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;

    @Value("${server.port}")
    private String serverPort;

    @Value("${short-link.stats.locale.amap-key}")
    private String amapKey;
    
    @Value("${short-link.domain.default}")
    private String domain;

    @Override
    public ShortLinkAddRespDTO addShortLink(ShortLinkAddReqDTO shortLinkAddReqDTO) {
        String suffix = generateSuffix(shortLinkAddReqDTO);
        ShortLinkDo shortLinkDo = ShortLinkDo
                .builder()
                .domain(domain)
                .originUrl(shortLinkAddReqDTO.getOriginUrl())
                .gid(shortLinkAddReqDTO.getGid())
                .favicon(getFavicon(shortLinkAddReqDTO.getOriginUrl()))
                .createType(shortLinkAddReqDTO.getCreateType())
                .validDateType(shortLinkAddReqDTO.getValidDateType())
                .validDate(shortLinkAddReqDTO.getValidDate())
                .description(shortLinkAddReqDTO.getDescription())
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .clickNum(0)
                .build();
        String fullShortUrl = StrBuilder
                .create(domain)
                .append(":")
                .append(serverPort)
                .append("/")
                .append(suffix)
                .toString();

        shortLinkDo.setFullShortUrl(fullShortUrl);
        shortLinkDo.setShortUrl(suffix);
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .gid(shortLinkAddReqDTO.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDo);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException e) {
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ClientException(SHORT_ADD_REPEAT_ERROR);
        }
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl),
                shortLinkDo.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(shortLinkDo.getValidDate()),TimeUnit.MILLISECONDS);
        shortLinkCreateBloomFilter.add(fullShortUrl);
        return ShortLinkAddRespDTO
                .builder()
                .originUrl(shortLinkAddReqDTO.getOriginUrl())
                .fullShortUrl(shortLinkDo.getFullShortUrl())
                .gid(shortLinkAddReqDTO.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
//        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
//                .eq(ShortLinkDo::getGid, shortLinkPageReqDTO.getGid())
//                .eq(ShortLinkDo::getEnableStatus, 0)
//                .eq(ShortLinkDo::getDelFlag, 0);
//        IPage<ShortLinkDo> resultPage = baseMapper.selectPage(shortLinkPageReqDTO, queryWrapper);
        IPage<ShortLinkDo> shortLinkDoIPage = baseMapper.pageLink(shortLinkPageReqDTO);
        return shortLinkDoIPage.convert(item->BeanUtil.convert(item, ShortLinkPageRespDTO.class));

    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDo> wrapper = new QueryWrapper<>();
        wrapper.select("gid,count(*) as shortLinkCount");
        wrapper.in("gid", requestParam);
        wrapper.eq("enable_status", 0);
        wrapper.eq("del_flag", 0);
        wrapper.groupBy("gid");
        List<Map<String, Object>> selectMaps = baseMapper.selectMaps(wrapper);
        return BeanUtil.convert(selectMaps, ShortLinkGroupCountQueryRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO shortLinkUpdateReqDTO) {
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, shortLinkUpdateReqDTO.getOriginGid())
                .eq(ShortLinkDo::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                .eq(ShortLinkDo::getDelFlag, 0)
                .eq(ShortLinkDo::getEnableStatus, 0);
        ShortLinkDo hasShortLinkDo = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDo == null) {
            throw new ClientException(SHORT_NOT_EXIST);
        }
        if (Objects.equals(hasShortLinkDo.getGid(), shortLinkUpdateReqDTO.getGid())) {
            LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                    .eq(ShortLinkDo::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                    .eq(ShortLinkDo::getGid, shortLinkUpdateReqDTO.getGid())
                    .eq(ShortLinkDo::getDelFlag, 0)
                    .eq(ShortLinkDo::getEnableStatus, 0)
                    .set(Objects.equals(shortLinkUpdateReqDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDo::getValidDate, null);
            ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                    .domain(hasShortLinkDo.getDomain())
                    .shortUrl(hasShortLinkDo.getShortUrl())
                    .favicon(hasShortLinkDo.getFavicon())
                    .createType(hasShortLinkDo.getCreateType())
                    .gid(hasShortLinkDo.getGid())
                    .originUrl(shortLinkUpdateReqDTO.getOriginUrl())
                    .description(shortLinkUpdateReqDTO.getDescription())
                    .validDateType(shortLinkUpdateReqDTO.getValidDateType())
                    .validDate(shortLinkUpdateReqDTO.getValidDate())
                    .build();
            baseMapper.update(shortLinkDo, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LOCK_GID_UPDATE_KEY);
            RLock lock = readWriteLock.readLock();
            if(!lock.tryLock()){
                throw new ClientException("短链接正在被访问，请稍后在试");
            }
            try {
                //修改短链接
                LambdaUpdateWrapper<ShortLinkDo> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                        .eq(ShortLinkDo::getFullShortUrl, hasShortLinkDo.getFullShortUrl())
                        .eq(ShortLinkDo::getGid, hasShortLinkDo.getGid())
                        .eq(ShortLinkDo::getDelFlag, 0)
                        .eq(ShortLinkDo::getDelTime, 0L)
                        .eq(ShortLinkDo::getEnableStatus, 0);
                ShortLinkDo delShortLinkDO = ShortLinkDo.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                        .domain(hasShortLinkDo.getDomain())
                        .shortUrl(hasShortLinkDo.getShortUrl())
                        .fullShortUrl(hasShortLinkDo.getFullShortUrl())
                        .favicon(hasShortLinkDo.getFavicon())
                        .createType(hasShortLinkDo.getCreateType())
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .clickNum(hasShortLinkDo.getClickNum())
                        .enableStatus(hasShortLinkDo.getEnableStatus())
                        .createType(hasShortLinkDo.getCreateType())
                        .totalUip(hasShortLinkDo.getTotalUip())
                        .totalUv(hasShortLinkDo.getTotalUv())
                        .totalPv(hasShortLinkDo.getTotalPv())
                        .originUrl(shortLinkUpdateReqDTO.getOriginUrl())
                        .description(shortLinkUpdateReqDTO.getDescription())
                        .validDateType(shortLinkUpdateReqDTO.getValidDateType())
                        .validDate(shortLinkUpdateReqDTO.getValidDate())
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDo);

                //修改短链接路由
                LambdaQueryWrapper<ShortLinkGotoDO> shortLinkGotoDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDo.getGid())
                        .eq(ShortLinkGotoDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(shortLinkGotoDOLambdaQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(shortLinkUpdateReqDTO.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);

                //修改短链接今日统计
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayDTOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayList = linkStatsTodayMapper.selectList(statsTodayDTOLambdaQueryWrapper);
                if(CollUtil.isNotEmpty(linkStatsTodayList)){
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .collect(Collectors.toList()));
                    linkStatsTodayList.forEach(each->{
                        each.setGid(shortLinkUpdateReqDTO.getGid());
                    });
                    linkStatsTodayService.saveBatch(linkStatsTodayList);
                }

                //修改短链接基础访问监控
                LambdaUpdateWrapper<LinkAccessStatsDO> LinkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO,LinkAccessStatsUpdateWrapper);

                //修改短链接访问日志
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO,linkAccessLogsUpdateWrapper);

                //修改短链接地区统计访问
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, hasShortLinkDo.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO,linkLocaleStatsUpdateWrapper);

                //修改短链接设备访问统计
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, hasShortLinkDo.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO,linkDeviceStatsUpdateWrapper);

                //修改短链接网络访问统计
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, hasShortLinkDo.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO,linkNetworkStatsUpdateWrapper);

                //修改短链接操作系统访问统计
                LambdaUpdateWrapper<LinkOSStatsDO> linkOSStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOSStatsDO.class)
                        .eq(LinkOSStatsDO::getFullShortUrl, hasShortLinkDo.getFullShortUrl())
                        .eq(LinkOSStatsDO::getGid, hasShortLinkDo.getGid())
                        .eq(LinkOSStatsDO::getDelFlag, 0);
                LinkOSStatsDO linkOSStatsDO = LinkOSStatsDO.builder()
                        .gid(shortLinkUpdateReqDTO.getGid())
                        .build();
                linkOSStatsMapper.update(linkOSStatsDO,linkOSStatsUpdateWrapper);


            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        if(!Objects.equals(hasShortLinkDo.getValidDateType(),shortLinkUpdateReqDTO.getValidDateType())
            ||!Objects.equals(hasShortLinkDo.getValidDate(), shortLinkUpdateReqDTO.getValidDate())){
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, shortLinkUpdateReqDTO.getFullShortUrl()));
            if(hasShortLinkDo.getValidDate()!=null&&hasShortLinkDo.getValidDate().before(new Date())){
                if (Objects.equals(shortLinkUpdateReqDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) || shortLinkUpdateReqDTO.getValidDate().after(new Date())) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, shortLinkUpdateReqDTO.getFullShortUrl()));
                }
            }
        }
    }

    @Override
    public void restoreUrl(String shortUrl, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + ":" +serverPort+"/" + shortUrl;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
        if (!StringUtils.isBlank(originalLink)) {
            shortLinkStats(null,statsRecord);
            sendRedirect(response, originalLink);
            return;
        }
        boolean hasFullShortLink = shortLinkCreateBloomFilter.contains(fullShortUrl);
        if(!hasFullShortLink){
            sendRedirect(response, "/page/notfound");
            return;
        }
        String hasFullShortLinkNullValue = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if(!StringUtils.isBlank(hasFullShortLinkNullValue)){
            sendRedirect(response, "/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (!StringUtils.isBlank(originalLink)) {
                shortLinkStats(null,statsRecord);
                sendRedirect(response, originalLink);
                return;
            }
            LambdaQueryWrapper<ShortLinkGotoDO> shortLinkGotoDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(shortLinkGotoDOLambdaQueryWrapper);
            if (shortLinkGotoDO == null) {
                sendRedirect(response, "/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDo> shortLinkDoLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                    .eq(ShortLinkDo::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDo::getFullShortUrl, shortLinkGotoDO.getFullShortUrl())
                    .eq(ShortLinkDo::getDelFlag, 0)
                    .eq(ShortLinkDo::getEnableStatus, 0);
            ShortLinkDo shortLinkDo = baseMapper.selectOne(shortLinkDoLambdaQueryWrapper);

            if (shortLinkDo == null||(shortLinkDo.getValidDate()!=null&&shortLinkDo.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),"-",30, TimeUnit.SECONDS);
                sendRedirect(response, "/page/notfound");
                return;
            }
            originalLink = shortLinkDo.getOriginUrl();
            stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl),
                    originalLink,
                    LinkUtil.getLinkCacheValidTime(shortLinkDo.getValidDate()),TimeUnit.MILLISECONDS);
            shortLinkStats(shortLinkGotoDO.getGid(),statsRecord);
            sendRedirect(response,originalLink);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public ShortLinkBatchCreateRespDTO batchAddShortLink(ShortLinkBatchCreateReqDTO shortLinkBatchCreateReqDTO) {
        List<String> originUrls = shortLinkBatchCreateReqDTO.getOriginUrls();
        List<String> descriptions = shortLinkBatchCreateReqDTO.getDescriptions();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for(int i=0;i<originUrls.size();i++){
            ShortLinkAddReqDTO shortLinkAddReqDTO = BeanUtil.convert(shortLinkBatchCreateReqDTO, ShortLinkAddReqDTO.class);
            shortLinkAddReqDTO.setDescription(descriptions.get(i));
            shortLinkAddReqDTO.setOriginUrl(originUrls.get(i));
            try {
                ShortLinkAddRespDTO shortLinkAddRespDTO = addShortLink(shortLinkAddReqDTO);
                ShortLinkBaseInfoRespDTO shortLinkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLinkAddRespDTO.getFullShortUrl())
                        .originUrl(shortLinkAddRespDTO.getOriginUrl())
                        .description(descriptions.get(i))
                        .build();
                result.add(shortLinkBaseInfoRespDTO);
            } catch (Exception e) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .baseLinkInfos(result)
                .total(result.size())
                .build();
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest request=((HttpServletRequest) servletRequest);
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        AtomicBoolean uvFirstFlag=new AtomicBoolean();
        AtomicReference<String> uv=new AtomicReference<>();
        Cookie[] cookies = request.getCookies();
        Runnable addResponseCookieTask=()->{
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60*60*24*30);
            uvCookie.setPath(StringUtil.substring(fullShortUrl,fullShortUrl.lastIndexOf("/"),fullShortUrl.length()));
            response.addCookie(uvCookie);
            uvFirstFlag.set(true);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV + fullShortUrl, uv.get());
        };

        String actualIpAddress = LinkUtil.getActualIp(request);
        Long uipAddCount = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP + fullShortUrl, actualIpAddress);
        boolean uipFirstFlag=uipAddCount>0?true:false;
        if(ArrayUtils.isNotEmpty(cookies)){
            Arrays.stream(cookies)
                    .filter(each->Objects.equals(each.getName(),"uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each->{
                        Long add = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV + fullShortUrl, each);
                        uv.set(each);
                        uvFirstFlag.set(add!=null&&add>0L);
                    },addResponseCookieTask);
        }else{
            addResponseCookieTask.run();
        }
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .remoteAddr(actualIpAddress)
                .uipFirstFlag(uipFirstFlag)
                .uvFirstFlag(uvFirstFlag.get())
                .os(LinkUtil.getOs(request))
                .network(LinkUtil.getNetwork(request))
                .device(LinkUtil.getDevice(request))
                .browser(LinkUtil.getBrowser(request))
                .uv(uv.get())
                .build();
    }

    @Override
    public void shortLinkStats(String gid, ShortLinkStatsRecordDTO statsRecord){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LOCK_GID_UPDATE_KEY);
        RLock lock = readWriteLock.readLock();
        if(!lock.tryLock()){
            //mq异步统计
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try {
            if(StringUtils.isBlank(gid)){
                LambdaQueryWrapper<ShortLinkGotoDO> LinkGotoDOQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, statsRecord.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getDelFlag,0);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(LinkGotoDOQueryWrapper);
                gid=shortLinkGotoDO.getGid();
            }
            Date date = new Date();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag()?1:0)
                    .uip(statsRecord.getUipFirstFlag()?1:0)
                    .date(date)
                    .weekday(DateUtil.dayOfWeekEnum(date).getIso8601Value())
                    .hour(DateUtil.hour(date,true))
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String,Object> localeParamMap=new HashMap<>();
            localeParamMap.put("key",amapKey);
            localeParamMap.put("ip",statsRecord.getRemoteAddr());
            String linkLocaleJsonResult = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject linkLocaleJsonResultObj = JSON.parseObject(linkLocaleJsonResult);
            String infocode = linkLocaleJsonResultObj.getString("infocode");
            String actualProvince="未知";
            String actualCity="未知";
            if(StrUtil.isNotBlank(infocode)&&StrUtil.equals(infocode,"10000")){
                String province = linkLocaleJsonResultObj.getString("province");
                actualProvince=StringUtil.equals(province,"[]")?actualProvince:province;
                String city = linkLocaleJsonResultObj.getString("city");
                actualCity = StringUtil.equals(city,"[]")?actualCity:city;
                String adcode = linkLocaleJsonResultObj.getString("adcode");
                adcode=StringUtil.equals(adcode,"[]")?"未知":adcode;
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince)
                        .city(actualCity)
                        .adcode(adcode)
                        .cnt(1)
                        .fullShortUrl(statsRecord.getFullShortUrl())
                        .country("中国")
                        .gid(gid)
                        .date(date)
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleStat(linkLocaleStatsDO);
            }
            LinkOSStatsDO linkOSStatsDO = LinkOSStatsDO.builder()
                    .cnt(1)
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .date(date)
                    .os(statsRecord.getOs())
                    .build();
            linkOSStatsMapper.shortLinkOsState(linkOSStatsDO);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .cnt(1)
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .date(date)
                    .browser(statsRecord.getBrowser())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserStat(linkBrowserStatsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO
                    .builder()
                    .cnt(1)
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .date(date)
                    .device(statsRecord.getDevice())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceStat(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO
                    .builder()
                    .cnt(1)
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .date(date)
                    .network(statsRecord.getNetwork())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkStat(linkNetworkStatsDO);
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO
                    .builder()
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .ip(statsRecord.getRemoteAddr())
                    .network(statsRecord.getNetwork())
                    .os(statsRecord.getOs())
                    .device(statsRecord.getDevice())
                    .browser(statsRecord.getBrowser())
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .user(statsRecord.getUv())
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            baseMapper.incrementStats(gid, statsRecord.getFullShortUrl(), 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .todayPv(1)
                    .todayUv(statsRecord.getUvFirstFlag()?1:0)
                    .todayUip(statsRecord.getUipFirstFlag()?1:0)
                    .fullShortUrl(statsRecord.getFullShortUrl())
                    .gid(gid)
                    .date(date)
                    .build();
            linkStatsTodayMapper.shortTodayLinkStat(linkStatsTodayDO);
        } catch (Exception e) {
            log.error("短链接访问量统计异常", e);
        }finally {
            lock.unlock();
        }

    }

    private String generateSuffix(ShortLinkAddReqDTO shortLinkAddReqDTO) {
        String suffix;
        int count = 10;
        while (true) {
            if (count == 0) {
                throw new ClientException(SHORT_ADD_ERROR);
            }
            suffix = HashUtil.hashToBase62(shortLinkAddReqDTO.getOriginUrl() + System.currentTimeMillis());
            String fullShortUrl = domain + "/" + suffix;
            boolean hasSuffix = shortLinkCreateBloomFilter.contains(fullShortUrl);
            if (!hasSuffix) {
                break;
            }
            count--;
        }
        return suffix;
    }

    private void sendRedirect(ServletResponse response, String originalLink) {
        try {
            ((HttpServletResponse) response).sendRedirect(originalLink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
