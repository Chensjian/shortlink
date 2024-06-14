package com.chen.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {
    @Insert("INSERT INTO t_link_locale_stats (full_short_url, gid, date, cnt, country, province, city, adcode, create_time, update_time, del_flag) " +
            "VALUES( #{linkLocaleStats.fullShortUrl}, #{linkLocaleStats.gid}, #{linkLocaleStats.date}, #{linkLocaleStats.cnt}, #{linkLocaleStats.country}, #{linkLocaleStats.province}, #{linkLocaleStats.city}, #{linkLocaleStats.adcode}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkLocaleStats.cnt};")
    void shortLinkLocaleStat(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStats);

    @Select("select province,sum(cnt) as  cnt " +
            "from t_link_locale_stats " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid,province")
    List<LinkLocaleStatsDO> linkLocaleStatsMapper(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReq);

    @Select("select province,sum(cnt) as  cnt " +
            "from t_link_locale_stats " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid,province")
    List<LinkLocaleStatsDO> groupLinkLocaleStatsMapper(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}
