package com.chen.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.chen.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接访问监控持久层
 */
@Mapper
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag) " +
            "VALUES( #{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0) ON DUPLICATE KEY UPDATE pv = pv +  #{linkAccessStats.pv}, " +
            "uv = uv + #{linkAccessStats.uv}, " +
            " uip = uip + #{linkAccessStats.uip};")
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);

    @Select("select date, sum(pv) as pv,sum(uv) as uv,sum(uip) as uip " +
            "from t_link_access_stats " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and date between #{param.startDate}  and #{param.endDate} " +
            "group by full_short_url,gid,`date`")
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReq);

    @Select("select `hour`,sum(pv) as pv " +
            "from t_link_access_stats " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid,`hour`")
    List<LinkAccessStatsDO> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    @Select("select weekday,sum(pv) as pv " +
            "from t_link_access_stats " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid,weekday")
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    @Select("select date, sum(pv) as pv,sum(uv) as uv,sum(uip) as uip " +
            "from t_link_access_stats " +
            "where gid=#{param.gid} " +
            "and date between #{param.startDate}  and #{param.endDate} " +
            "group by gid,`date`")
    List<LinkAccessStatsDO> groupListStatsByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    @Select("select `hour`,sum(pv) as pv " +
            "from t_link_access_stats " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid,`hour`")
    List<LinkAccessStatsDO> groupListHourStatsByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    @Select("select weekday,sum(pv) as pv " +
            "from t_link_access_stats " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid,weekday")
    List<LinkAccessStatsDO> groupListWeekdayStatsByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}
