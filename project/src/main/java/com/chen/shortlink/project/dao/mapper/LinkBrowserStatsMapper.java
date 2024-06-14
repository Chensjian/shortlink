package com.chen.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.chen.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.chen.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 浏览器统计持久层
 */
@Mapper
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {

    @Insert("INSERT INTO t_link_browser_stats (full_short_url, gid, date, cnt, browser,create_time, update_time, del_flag) " +
            "VALUES( #{linkBrowserStats.fullShortUrl}, #{linkBrowserStats.gid}, #{linkBrowserStats.date}, #{linkBrowserStats.cnt}, #{linkBrowserStats.browser}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkBrowserStats.cnt};")
    void shortLinkBrowserStat(@Param("linkBrowserStats") LinkBrowserStatsDO linkBrowserStats);

    @Select("select browser ,sum(cnt) as cnt " +
            "from t_link_browser_stats " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid,browser")
    List<LinkBrowserStatsDO> listBrowserStatsByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);


    @Select("select browser ,sum(cnt) as cnt " +
            "from t_link_browser_stats " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid,browser")
    List<LinkBrowserStatsDO> groupListBrowserStatsByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}
