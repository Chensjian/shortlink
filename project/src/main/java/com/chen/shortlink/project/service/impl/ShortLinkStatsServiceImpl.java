package com.chen.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chen.shortlink.project.dao.entity.*;
import com.chen.shortlink.project.dao.mapper.*;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chen.shortlink.project.dto.resp.*;
import com.chen.shortlink.project.service.ShortLinkStatsService;
import com.chen.shortlink.project.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 短链接监控接口实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOSStatsMapper linkOSStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;


    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        List<LinkAccessStatsDO> linkAccessStatsList=linkAccessStatsMapper.listStatsByShortLink(shortLinkStatsReqDTO);
        if(CollUtil.isEmpty(linkAccessStatsList)){
            return null;
        }
        // 基础访问数据
        LinkAccessStatsDO pvUvUidStatsByShortLink=linkAccessLogsMapper.findPvUvUipStatsByShortLink(shortLinkStatsReqDTO);
        // 基础访问详情
        List<ShortLinkStatsAccessDailyRespDTO> daily=new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(shortLinkStatsReqDTO.getStartDate()), DateUtil.parse(shortLinkStatsReqDTO.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        rangeDates.forEach(each->linkAccessStatsList.stream()
                .filter(item-> Objects.equals(each,DateUtil.formatDate(item.getDate())))
                .findFirst()
                .ifPresentOrElse(item->{
                    ShortLinkStatsAccessDailyRespDTO linkStatsAccessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .uv(item.getUv())
                            .pv(item.getPv())
                            .uip(item.getUip())
                            .build();
                    daily.add(linkStatsAccessDailyRespDTO);
                },()->{
                    ShortLinkStatsAccessDailyRespDTO linkStatsAccessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .uv(0)
                            .pv(0)
                            .uip(0)
                            .build();
                    daily.add(linkStatsAccessDailyRespDTO);
                })
        );
        // 地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        List<LinkLocaleStatsDO> linkLocaleStatsList=linkLocaleStatsMapper.linkLocaleStatsMapper(shortLinkStatsReqDTO);
        int localeCntSum=linkLocaleStatsList.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        linkLocaleStatsList.forEach(each->{
            double ratio=(double)each.getCnt()/localeCntSum;
            double actualRatio=Math.round(ratio*100.0)/100.0;
            ShortLinkStatsLocaleCNRespDTO shortLinkStatsLocaleCNRespDTO = ShortLinkStatsLocaleCNRespDTO
                    .builder()
                    .locale(each.getProvince())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            localeCnStats.add(shortLinkStatsLocaleCNRespDTO);
        });
        // 小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsList=linkAccessStatsMapper.listHourStatsByShortLink(shortLinkStatsReqDTO);
        for(int i=0;i<24;i++){
            AtomicInteger hour = new AtomicInteger(i);
            Integer hourCnt = listHourStatsList.stream()
                    .filter(item -> Objects.equals(hour.get(), item.getHour()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问ip详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats=new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink=linkAccessLogsMapper.listTopIpByShortLink(shortLinkStatsReqDTO);
        listTopIpByShortLink.forEach(each->{
            ShortLinkStatsTopIpRespDTO shortLinkStatsTopIpRespDTO = ShortLinkStatsTopIpRespDTO
                    .builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .build();
            topIpStats.add(shortLinkStatsTopIpRespDTO);
        });

        // 一周访问详情
        List<Integer> weekdayStats=new ArrayList<>();
        List<LinkAccessStatsDO> listWeekdayStatsByShortLink=linkAccessStatsMapper.listWeekdayStatsByShortLink(shortLinkStatsReqDTO);
        for(int i=1;i<=7;i++){
            AtomicInteger weekDay=new AtomicInteger(i);
            Integer weekdayPvCount = listWeekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(weekDay.get(), each.getWeekday()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv).orElse(0);
            weekdayStats.add(weekdayPvCount);
        }
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats=new ArrayList<>();
        List<LinkBrowserStatsDO> linkBrowserStatsList=linkBrowserStatsMapper.listBrowserStatsByShortLink(shortLinkStatsReqDTO);
        int browserCntSum = linkBrowserStatsList.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        linkBrowserStatsList.forEach(each->{
            double ration=(double)each.getCnt()/browserCntSum;
            double actualRatio=Math.round(ration*100.0)/100.0;
            ShortLinkStatsBrowserRespDTO shortLinkStatsBrowserRespDTO = ShortLinkStatsBrowserRespDTO
                    .builder()
                    .browser(each.getBrowser())
                    .ratio(actualRatio)
                    .cnt(each.getCnt())
                    .build();
            browserStats.add(shortLinkStatsBrowserRespDTO);

        });
        // 操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osStats=new ArrayList<>();
        List<Map<String,Object>> linkOSStatsDOList=linkOSStatsMapper.listOsStatsByShortLink(shortLinkStatsReqDTO);
        int osCntSum = linkOSStatsDOList.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        linkOSStatsDOList.forEach(each->{
            int cnt = Integer.parseInt(each.get("cnt").toString());
            double ration=(double)cnt/osCntSum;
            double actualRatio=Math.round(ration*100)/100.0;
            ShortLinkStatsOsRespDTO shortLinkStatsOsRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .os(each.get("os").toString())
                    .cnt(cnt)
                    .ratio(actualRatio).build();
            osStats.add(shortLinkStatsOsRespDTO);
        });
        // 访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeStats=new ArrayList<>();
        Map<String,Object> uvTypeCntMap=linkAccessLogsMapper.findUvTypeCntByShortLink(shortLinkStatsReqDTO);
        int oldUserCnt=Integer.parseInt(
                Optional.ofNullable(uvTypeCntMap)
                        .map(each->each.get("oldUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int newUserCnt=Integer.parseInt(
                Optional.ofNullable(uvTypeCntMap)
                        .map(each->each.get("newUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int uvSum=oldUserCnt+newUserCnt;
        double oldRatio=(double)oldUserCnt/uvSum;
        double actualOldRatio=Math.round(oldRatio*100.0)/100.0;
        double newRatio=(double)newUserCnt/uvSum;
        double actualNewRatio=Math.round(newRatio*100.0)/100.0;
        ShortLinkStatsUvRespDTO newUvRespDTO = ShortLinkStatsUvRespDTO
                .builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        ShortLinkStatsUvRespDTO oldUvRespDTO = ShortLinkStatsUvRespDTO
                .builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStats.add(newUvRespDTO);
        uvTypeStats.add(oldUvRespDTO);

        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats=new ArrayList<>();
        List<Map<String,Object>> linkDeviceStatsList=linkDeviceStatsMapper.listDeviceStatsByShortLink(shortLinkStatsReqDTO);
        Integer deviceCntSum = linkDeviceStatsList.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        linkDeviceStatsList.forEach(each->{
            int cnt = Integer.parseInt(each.get("cnt").toString());
            double ratio=(double)cnt / deviceCntSum;
            double actualRatio=Math.round(ratio*100.0)/100.0;
            ShortLinkStatsDeviceRespDTO linkStatsDeviceRespDTO = ShortLinkStatsDeviceRespDTO
                    .builder()
                    .device(each.get("device").toString())
                    .ratio(actualRatio)
                    .cnt(cnt)
                    .build();
            deviceStats.add(linkStatsDeviceRespDTO);
        });
        // 访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkStats=new ArrayList<>();
        List<Map<String,Object>> linkNetworkStatsList=linkNetworkStatsMapper.listNetworkStatsByShortLink(shortLinkStatsReqDTO);
        int networkCntSum = linkNetworkStatsList.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        linkNetworkStatsList.forEach(each->{
            int cnt=Integer.parseInt(each.get("cnt").toString());
            double ratio=(double)cnt/networkCntSum;
            double actualRatio=Math.round(ratio*100.0)/100.0;
            ShortLinkStatsNetworkRespDTO linkStatsNetworkRespDTO = ShortLinkStatsNetworkRespDTO
                    .builder()
                    .cnt(cnt)
                    .network(each.get("network").toString())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(linkStatsNetworkRespDTO);
        });

        return ShortLinkStatsRespDTO.builder()
                .pv(pvUvUidStatsByShortLink.getPv())
                .uv(pvUvUidStatsByShortLink.getUv())
                .uip(pvUvUidStatsByShortLink.getUip())
                .daily(daily)
                .localeCNStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .deviceStats(deviceStats)
                .uvTypeStats(uvTypeStats)
                .osStats(osStats)
                .networkStats(networkStats)
                .build();
    }

    @Override
    public ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO) {
        List<LinkAccessStatsDO> linkAccessStatsList=linkAccessStatsMapper.groupListStatsByShortLink(shortLinkGroupStatsReqDTO);
        if(CollUtil.isEmpty(linkAccessStatsList)){
            return null;
        }
        // 基础访问数据
        LinkAccessStatsDO pvUvUidStatsByShortLink=linkAccessLogsMapper.groupFindPvUvUipStatsByShortLink(shortLinkGroupStatsReqDTO);
        // 基础访问详情
        List<ShortLinkStatsAccessDailyRespDTO> daily=new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(shortLinkGroupStatsReqDTO.getStartDate()), DateUtil.parse(shortLinkGroupStatsReqDTO.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        rangeDates.forEach(each->linkAccessStatsList.stream()
                .filter(item-> Objects.equals(each,DateUtil.formatDate(item.getDate())))
                .findFirst()
                .ifPresentOrElse(item->{
                    ShortLinkStatsAccessDailyRespDTO linkStatsAccessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .uv(item.getUv())
                            .pv(item.getPv())
                            .uip(item.getUip())
                            .build();
                    daily.add(linkStatsAccessDailyRespDTO);
                },()->{
                    ShortLinkStatsAccessDailyRespDTO linkStatsAccessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .uv(0)
                            .pv(0)
                            .uip(0)
                            .build();
                    daily.add(linkStatsAccessDailyRespDTO);
                })
        );
        // 地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        List<LinkLocaleStatsDO> linkLocaleStatsList=linkLocaleStatsMapper.groupLinkLocaleStatsMapper(shortLinkGroupStatsReqDTO);
        int localeCntSum=linkLocaleStatsList.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        linkLocaleStatsList.forEach(each->{
            double ratio=(double)each.getCnt()/localeCntSum;
            double actualRatio=Math.round(ratio*100.0)/100.0;
            ShortLinkStatsLocaleCNRespDTO shortLinkStatsLocaleCNRespDTO = ShortLinkStatsLocaleCNRespDTO
                    .builder()
                    .locale(each.getProvince())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            localeCnStats.add(shortLinkStatsLocaleCNRespDTO);
        });
        // 小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsList=linkAccessStatsMapper.groupListHourStatsByShortLink(shortLinkGroupStatsReqDTO);
        for(int i=0;i<24;i++){
            AtomicInteger hour = new AtomicInteger(i);
            Integer hourCnt = listHourStatsList.stream()
                    .filter(item -> Objects.equals(hour.get(), item.getHour()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问ip详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats=new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink=linkAccessLogsMapper.groupListTopIpByShortLink(shortLinkGroupStatsReqDTO);
        listTopIpByShortLink.forEach(each->{
            ShortLinkStatsTopIpRespDTO shortLinkStatsTopIpRespDTO = ShortLinkStatsTopIpRespDTO
                    .builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .build();
            topIpStats.add(shortLinkStatsTopIpRespDTO);
        });

        // 一周访问详情
        List<Integer> weekdayStats=new ArrayList<>();
        List<LinkAccessStatsDO> listWeekdayStatsByShortLink=linkAccessStatsMapper.groupListWeekdayStatsByShortLink(shortLinkGroupStatsReqDTO);
        for(int i=1;i<=7;i++){
            AtomicInteger weekDay=new AtomicInteger(i);
            Integer weekdayPvCount = listWeekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(weekDay.get(), each.getWeekday()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv).orElse(0);
            weekdayStats.add(weekdayPvCount);
        }
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats=new ArrayList<>();
        List<LinkBrowserStatsDO> linkBrowserStatsList=linkBrowserStatsMapper.groupListBrowserStatsByShortLink(shortLinkGroupStatsReqDTO);
        int browserCntSum = linkBrowserStatsList.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        linkBrowserStatsList.forEach(each->{
            double ration=(double)each.getCnt()/browserCntSum;
            double actualRatio=Math.round(ration*100.0)/100.0;
            ShortLinkStatsBrowserRespDTO shortLinkStatsBrowserRespDTO = ShortLinkStatsBrowserRespDTO
                    .builder()
                    .browser(each.getBrowser())
                    .ratio(actualRatio)
                    .cnt(each.getCnt())
                    .build();
            browserStats.add(shortLinkStatsBrowserRespDTO);

        });
        // 操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osStats=new ArrayList<>();
        List<Map<String,Object>> linkOSStatsDOList=linkOSStatsMapper.groupListOsStatsByShortLink(shortLinkGroupStatsReqDTO);
        int osCntSum = linkOSStatsDOList.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        linkOSStatsDOList.forEach(each->{
            int cnt = Integer.parseInt(each.get("cnt").toString());
            double ration=(double)cnt/osCntSum;
            double actualRatio=Math.round(ration*100)/100.0;
            ShortLinkStatsOsRespDTO shortLinkStatsOsRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .os(each.get("os").toString())
                    .cnt(cnt)
                    .ratio(actualRatio).build();
            osStats.add(shortLinkStatsOsRespDTO);
        });
        // 访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeStats=new ArrayList<>();
        Map<String,Object> uvTypeCntMap=linkAccessLogsMapper.groupFindUvTypeCntByShortLink(shortLinkGroupStatsReqDTO);
        int oldUserCnt=Integer.parseInt(
                Optional.ofNullable(uvTypeCntMap)
                        .map(each->each.get("oldUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int newUserCnt=Integer.parseInt(
                Optional.ofNullable(uvTypeCntMap)
                        .map(each->each.get("newUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int uvSum=oldUserCnt+newUserCnt;
        double oldRatio=(double)oldUserCnt/uvSum;
        double actualOldRatio=Math.round(oldRatio*100.0)/100.0;
        double newRatio=(double)newUserCnt/uvSum;
        double actualNewRatio=Math.round(newRatio*100.0)/100.0;
        ShortLinkStatsUvRespDTO newUvRespDTO = ShortLinkStatsUvRespDTO
                .builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        ShortLinkStatsUvRespDTO oldUvRespDTO = ShortLinkStatsUvRespDTO
                .builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStats.add(newUvRespDTO);
        uvTypeStats.add(oldUvRespDTO);

        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats=new ArrayList<>();
        List<Map<String,Object>> linkDeviceStatsList=linkDeviceStatsMapper.groupListDeviceStatsByShortLink(shortLinkGroupStatsReqDTO);
        Integer deviceCntSum = linkDeviceStatsList.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        linkDeviceStatsList.forEach(each->{
            int cnt = Integer.parseInt(each.get("cnt").toString());
            double ratio=(double)cnt / deviceCntSum;
            double actualRatio=Math.round(ratio*100.0)/100.0;
            ShortLinkStatsDeviceRespDTO linkStatsDeviceRespDTO = ShortLinkStatsDeviceRespDTO
                    .builder()
                    .device(each.get("device").toString())
                    .ratio(actualRatio)
                    .cnt(cnt)
                    .build();
            deviceStats.add(linkStatsDeviceRespDTO);
        });
        // 访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkStats=new ArrayList<>();
        List<Map<String,Object>> linkNetworkStatsList=linkNetworkStatsMapper.groupListNetworkStatsByShortLink(shortLinkGroupStatsReqDTO);
        int networkCntSum = linkNetworkStatsList.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        linkNetworkStatsList.forEach(each->{
            int cnt=Integer.parseInt(each.get("cnt").toString());
            double ratio=(double)cnt/networkCntSum;
            double actualRatio=Math.round(ratio*100.0)/100.0;
            ShortLinkStatsNetworkRespDTO linkStatsNetworkRespDTO = ShortLinkStatsNetworkRespDTO
                    .builder()
                    .cnt(cnt)
                    .network(each.get("network").toString())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(linkStatsNetworkRespDTO);
        });

        return ShortLinkStatsRespDTO.builder()
                .pv(pvUvUidStatsByShortLink.getPv())
                .uv(pvUvUidStatsByShortLink.getUv())
                .uip(pvUvUidStatsByShortLink.getUip())
                .daily(daily)
                .localeCNStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .deviceStats(deviceStats)
                .uvTypeStats(uvTypeStats)
                .osStats(osStats)
                .networkStats(networkStats)
                .build();
    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO shortLinkStatsAccessRecordReqDTO) {
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getFullShortUrl, shortLinkStatsAccessRecordReqDTO.getFullShortUrl())
                .eq(LinkAccessLogsDO::getGid, shortLinkStatsAccessRecordReqDTO.getGid())
                .between(LinkAccessLogsDO::getCreateTime, shortLinkStatsAccessRecordReqDTO.getStartDate(), shortLinkStatsAccessRecordReqDTO.getEndDate())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(shortLinkStatsAccessRecordReqDTO, queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.convert(each, ShortLinkStatsAccessRecordRespDTO.class));
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();

        List<Map<String,Object>> uvTypeMap=linkAccessLogsMapper.selectUvTypeByUsers(
                shortLinkStatsAccessRecordReqDTO.getFullShortUrl(),
                shortLinkStatsAccessRecordReqDTO.getGid(),
                shortLinkStatsAccessRecordReqDTO.getStartDate(),
                shortLinkStatsAccessRecordReqDTO.getEndDate(),
                userAccessLogsList
        );
        actualResult.getRecords().forEach(each->{
            String uvType = uvTypeMap.stream()
                    .filter(item -> Objects.equals(each.getUser(), item.get("user")))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            each.setUvType(uvType);

        });
        return actualResult;
    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO shortLinkGroupStatsAccessRecordReqDTO) {
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getGid, shortLinkGroupStatsAccessRecordReqDTO.getGid())
                .between(LinkAccessLogsDO::getCreateTime, shortLinkGroupStatsAccessRecordReqDTO.getStartDate(), shortLinkGroupStatsAccessRecordReqDTO.getEndDate())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(shortLinkGroupStatsAccessRecordReqDTO, queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.convert(each, ShortLinkStatsAccessRecordRespDTO.class));
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();

        List<Map<String,Object>> uvTypeMap=linkAccessLogsMapper.selectGroupUvTypeByUsers(
                shortLinkGroupStatsAccessRecordReqDTO.getGid(),
                shortLinkGroupStatsAccessRecordReqDTO.getStartDate(),
                shortLinkGroupStatsAccessRecordReqDTO.getEndDate(),
                userAccessLogsList
        );
        actualResult.getRecords().forEach(each->{
            String uvType = uvTypeMap.stream()
                    .filter(item -> Objects.equals(each.getUser(), item.get("user")))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            each.setUvType(uvType);

        });
        return actualResult;
    }
}
