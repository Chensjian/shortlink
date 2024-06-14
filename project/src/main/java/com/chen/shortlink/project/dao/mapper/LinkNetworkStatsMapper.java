package com.chen.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.project.dao.entity.LinkDeviceStatsDO;
import com.chen.shortlink.project.dao.entity.LinkNetworkStatsDO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 访问网络统计持久层
 */
@Mapper
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    @Insert("INSERT INTO t_link_network_stats (full_short_url, gid, date, cnt, network,create_time, update_time, del_flag) " +
            "VALUES( #{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.gid}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkNetworkStats.cnt};")
    void shortLinkNetworkStat(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStats);


    @Select("select network ,sum(cnt) as cnt " +
            "from t_link_network_stats " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid,network")
    List<Map<String, Object>> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    @Select("select network ,sum(cnt) as cnt " +
            "from t_link_network_stats " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid,network")
    List<Map<String, Object>> groupListNetworkStatsByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}
