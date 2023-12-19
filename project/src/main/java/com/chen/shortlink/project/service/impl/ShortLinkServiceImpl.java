package com.chen.shortlink.project.service.impl;

import cn.hutool.core.annotation.Link;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
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
import com.chen.shortlink.project.common.convention.exception.ServiceException;
import com.chen.shortlink.project.common.enums.VailDateTypeEnum;
import com.chen.shortlink.project.dao.entity.*;
import com.chen.shortlink.project.dao.mapper.*;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chen.shortlink.project.service.ShortLinkGotoService;
import com.chen.shortlink.project.service.ShortLinkService;
import com.chen.shortlink.project.util.BeanUtil;
import com.chen.shortlink.project.util.HashUtil;
import com.chen.shortlink.project.util.LinkUtil;
import jakarta.servlet.ServletException;
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
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.chen.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.chen.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.chen.shortlink.project.common.convention.errorcode.BaseErrorCode.SERVICE_ERROR;
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

    @Value("${server.port}")
    private String serverPort;

    @Value("${short-link.stats.locale.amap-key}")
    private String amapKey;

    @Override
    public ShortLinkAddRespDTO addShortLink(ShortLinkAddReqDTO shortLinkAddReqDTO) {
        String suffix = generateSuffix(shortLinkAddReqDTO);
        ShortLinkDo shortLinkDo = ShortLinkDo
                .builder()
                .domain(shortLinkAddReqDTO.getDomain())
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
                .create(shortLinkAddReqDTO.getDomain())
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
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, shortLinkPageReqDTO.getGid())
                .eq(ShortLinkDo::getEnableStatus, 0)
                .eq(ShortLinkDo::getDelFlag, 0);
        IPage<ShortLinkDo> resultPage = baseMapper.selectPage(shortLinkPageReqDTO, queryWrapper);
        return resultPage.convert(item -> BeanUtil.convert(item, ShortLinkPageRespDTO.class));
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

    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO shortLinkAddReqDTO) {
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, shortLinkAddReqDTO.getOriginGid())
                .eq(ShortLinkDo::getFullShortUrl, shortLinkAddReqDTO.getFullShortUrl())
                .eq(ShortLinkDo::getDelFlag, 0)
                .eq(ShortLinkDo::getEnableStatus, 0);
        ShortLinkDo hasShortLinkDo = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDo == null) {
            throw new ClientException(SHORT_NOT_EXIST);
        }
        if (Objects.equals(hasShortLinkDo.getGid(), shortLinkAddReqDTO.getGid())) {
            LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                    .eq(ShortLinkDo::getFullShortUrl, shortLinkAddReqDTO.getFullShortUrl())
                    .eq(ShortLinkDo::getGid, shortLinkAddReqDTO.getGid())
                    .eq(ShortLinkDo::getDelFlag, 0)
                    .eq(ShortLinkDo::getEnableStatus, 0)
                    .set(Objects.equals(shortLinkAddReqDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDo::getValidDate, null);
            ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                    .domain(hasShortLinkDo.getDomain())
                    .shortUrl(hasShortLinkDo.getShortUrl())
                    .favicon(hasShortLinkDo.getFavicon())
                    .createType(hasShortLinkDo.getCreateType())
                    .gid(hasShortLinkDo.getGid())
                    .originUrl(shortLinkAddReqDTO.getOriginUrl())
                    .description(shortLinkAddReqDTO.getDescription())
                    .validDateType(shortLinkAddReqDTO.getValidDateType())
                    .validDate(shortLinkAddReqDTO.getValidDate())
                    .build();
            baseMapper.update(shortLinkDo, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDo> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                    .eq(ShortLinkDo::getFullShortUrl, shortLinkAddReqDTO.getFullShortUrl())
                    .eq(ShortLinkDo::getGid, shortLinkAddReqDTO.getGid())
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
                    .gid(hasShortLinkDo.getGid())
                    .clickNum(hasShortLinkDo.getClickNum())
                    .enableStatus(hasShortLinkDo.getEnableStatus())
                    .createType(hasShortLinkDo.getCreateType())
                    .originUrl(shortLinkAddReqDTO.getOriginUrl())
                    .description(shortLinkAddReqDTO.getDescription())
                    .validDateType(shortLinkAddReqDTO.getValidDateType())
                    .validDate(shortLinkAddReqDTO.getValidDate())
                    .delTime(0L)
                    .build();
            baseMapper.insert(shortLinkDo);
        }
    }

    @Override
    public void restoreUrl(String shortUrl, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + ":" +serverPort+"/" + shortUrl;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (!StringUtils.isBlank(originalLink)) {
            shortLinkStats(fullShortUrl,null,request,response);
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
                shortLinkStats(fullShortUrl,null,request,response);
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
            shortLinkStats(fullShortUrl,shortLinkDo.getGid(),request,response);
            sendRedirect(response,originalLink);
        } finally {
            lock.unlock();
        }

    }

    private void shortLinkStats(String fullShortUrl,String gid,ServletRequest servletRequest,ServletResponse servletResponse){
        AtomicBoolean uvFirstFlag=new AtomicBoolean();
        AtomicReference<String> uv=new AtomicReference<>();
        HttpServletRequest request=((HttpServletRequest) servletRequest);
        HttpServletResponse response=(HttpServletResponse)servletResponse;
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
        if(StringUtils.isBlank(gid)){
            LambdaQueryWrapper<ShortLinkGotoDO> LinkGotoDOQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkGotoDO::getDelFlag,0);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(LinkGotoDOQueryWrapper);
            gid=shortLinkGotoDO.getGid();
        }
        Date date = new Date();
        LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .pv(1)
                .uv(uvFirstFlag.get()?1:0)
                .uip(uipFirstFlag?1:0)
                .date(date)
                .weekday(DateUtil.dayOfWeekEnum(date).getIso8601Value())
                .hour(DateUtil.hour(date,true))
                .build();
        linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
        Map<String,Object> localeParamMap=new HashMap<>();
        localeParamMap.put("key",amapKey);
        localeParamMap.put("ip",actualIpAddress);
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
                    .fullShortUrl(fullShortUrl)
                    .country("中国")
                    .gid(gid)
                    .date(date)
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleStat(linkLocaleStatsDO);
        }
        LinkOSStatsDO linkOSStatsDO = LinkOSStatsDO.builder()
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(date)
                .os(LinkUtil.getOs(request))
                .build();
        linkOSStatsMapper.shortLinkOsState(linkOSStatsDO);
        LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(date)
                .browser(LinkUtil.getBrowser(request))
                .build();
        linkBrowserStatsMapper.shortLinkBrowserStat(linkBrowserStatsDO);
        LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO
                .builder()
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(date)
                .device(LinkUtil.getDevice(request))
                .build();
        linkDeviceStatsMapper.shortLinkDeviceStat(linkDeviceStatsDO);
        LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO
                .builder()
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(date)
                .network(LinkUtil.getNetwork(request))
                .build();
        linkNetworkStatsMapper.shortLinkNetworkStat(linkNetworkStatsDO);
        LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO
                .builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .ip(actualIpAddress)
                .network(LinkUtil.getNetwork(request))
                .os(LinkUtil.getOs(request))
                .device(LinkUtil.getDevice(request))
                .browser(LinkUtil.getBrowser(request))
                .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                .user(uv.get())
                .build();
        linkAccessLogsMapper.insert(linkAccessLogsDO);
        baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);
        LinkStatsTodayDTO linkStatsTodayDTO = LinkStatsTodayDTO.builder()
                .todayPv(1)
                .todayUv(uvFirstFlag.get()?1:0)
                .todayUip(uipFirstFlag?1:0)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(date)
                .build();
        linkStatsTodayMapper.shortTodayLinkStat(linkStatsTodayDTO);

    }

    private String generateSuffix(ShortLinkAddReqDTO shortLinkAddReqDTO) {
        String suffix;
        int count = 10;
        while (true) {
            if (count == 0) {
                throw new ClientException(SHORT_ADD_ERROR);
            }
            suffix = HashUtil.hashToBase62(shortLinkAddReqDTO.getOriginUrl() + System.currentTimeMillis());
            String fullShortUrl = shortLinkAddReqDTO.getDomain() + "/" + suffix;
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
