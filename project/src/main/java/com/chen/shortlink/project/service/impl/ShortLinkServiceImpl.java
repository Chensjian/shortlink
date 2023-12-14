package com.chen.shortlink.project.service.impl;

import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.common.convention.exception.ClientException;
import com.chen.shortlink.project.common.convention.exception.ServiceException;
import com.chen.shortlink.project.common.enums.VailDateTypeEnum;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.chen.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.chen.shortlink.project.dao.mapper.ShortLinkMapper;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.chen.shortlink.project.common.constant.RedisKeyConstant.*;
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
                .delTime(0L)
                .clickNum(0)
                .build();
        String fullShortUrl = StrBuilder
                .create(shortLinkAddReqDTO.getDomain())
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
        String fullShortUrl = serverName + "/" + shortUrl;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (!StringUtils.isBlank(originalLink)) {
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
        } finally {
            lock.unlock();
        }
        sendRedirect(response,originalLink);
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
