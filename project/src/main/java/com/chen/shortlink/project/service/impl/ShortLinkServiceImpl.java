package com.chen.shortlink.project.service.impl;

import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.common.convention.exception.ClientException;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dao.mapper.ShortLinkMapper;
import com.chen.shortlink.project.dto.req.ShortLinkAddReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chen.shortlink.project.service.ShortLinkService;
import com.chen.shortlink.project.util.BeanUtil;
import com.chen.shortlink.project.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.chen.shortlink.project.common.enums.ShortLinkErrorEnums.SHORT_ADD_ERROR;
import static com.chen.shortlink.project.common.enums.ShortLinkErrorEnums.SHORT_ADD_REPEAT_ERROR;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDo> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkCreateBloomFilter;

    @Override
    public ShortLinkAddRespDTO addShortLink(ShortLinkAddReqDTO shortLinkAddReqDTO) {
        String suffix = generateSuffix(shortLinkAddReqDTO);
        ShortLinkDo shortLinkDo = ShortLinkDo
                .builder()
                .domain(shortLinkAddReqDTO.getDomain())
                .originUrl(shortLinkAddReqDTO.getOriginUrl())
                .gid(shortLinkAddReqDTO.getGid())
                .favicon(shortLinkAddReqDTO.getFavicon())
                .createType(shortLinkAddReqDTO.getCreateType())
                .validDateType(shortLinkAddReqDTO.getValidDateType())
                .validDate(shortLinkAddReqDTO.getValidDate())
                .description(shortLinkAddReqDTO.getDescription())
                .build();
        String fullShortUrl=StrBuilder
                .create(shortLinkAddReqDTO.getDomain())
                .append("/")
                .append(suffix)
                .toString();

        shortLinkDo.setFullShortUrl(fullShortUrl);
        shortLinkDo.setShortUrl(suffix);
        try{
            baseMapper.insert(shortLinkDo);
        }catch (DuplicateKeyException e){
            log.warn("短链接：{} 重复入库",fullShortUrl);
            throw new ClientException(SHORT_ADD_REPEAT_ERROR);
        }
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
         return resultPage.convert(item->BeanUtil.convert(item, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
//        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
//                .in(ShortLinkDo::getGid, requestParam)
//                .eq(ShortLinkDo::getEnableStatus, 0)
//                .eq(ShortLinkDo::getDelFlag, 0)
//                .groupBy(ShortLinkDo::getGid)
//                .select(ShortLinkDo::getGid);
        QueryWrapper<ShortLinkDo> wrapper=new QueryWrapper<>();
        wrapper.select("gid,count(*) as shortLinkCount");
        wrapper.in("gid",requestParam);
        wrapper.eq("enable_status",0);
        wrapper.eq("del_flag",0);
        wrapper.groupBy("gid");
        List<Map<String, Object>> selectMaps = baseMapper.selectMaps(wrapper);
        return BeanUtil.convert(selectMaps,ShortLinkGroupCountQueryRespDTO.class);
    }

    private String generateSuffix(ShortLinkAddReqDTO shortLinkAddReqDTO){
        String suffix;
        int count=10;
        while(true){
            if(count==0){
                throw new ClientException(SHORT_ADD_ERROR);
            }
            suffix=HashUtil.hashToBase62(shortLinkAddReqDTO.getOriginUrl()+System.currentTimeMillis());
            String fullShortUrl=shortLinkAddReqDTO.getDomain()+"/"+suffix;
            boolean hasSuffix = shortLinkCreateBloomFilter.contains(fullShortUrl);
            if(!hasSuffix){
                break;
            }
            count--;
        }
        return suffix;
    }
}
