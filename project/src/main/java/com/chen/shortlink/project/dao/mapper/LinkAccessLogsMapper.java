package com.chen.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.chen.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.chen.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chen.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接访问统计持久层
 */
@Mapper
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {


    @Select("select count(user) as pv,count(distinct user) as uv,count(distinct ip) as uip " +
            "from t_link_access_logs " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid")
    LinkAccessStatsDO findPvUvUipStatsByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    @Select("select ip,count(ip) as cnt " +
            "from t_link_access_logs " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,gid,ip " +
            "order by cnt desc " +
            "limit 5 ")
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    @Select("select  sum(old_user) as oldUserCnt,sum(new_user) as newUserCnt " +
            " from (select  " +
            "case when count(distinct date(create_time))>1 then 1 else 0 end as old_user, " +
            "case when count(distinct date(create_time))=1 and max(create_time) >= #{param.startDate} and max(create_time)<=#{param.endDate} " +
            "then 1 else 0 end as new_user " +
            "from t_link_access_logs " +
            "where full_short_url=#{param.fullShortUrl} " +
            "and gid=#{param.gid} " +
            "GROUP BY user) " +
            "as user_counts")
    Map<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO shortLinkStatsReqDTO);


    @Select("select count(user) as pv,count(distinct user) as uv,count(distinct ip) as uip " +
            "from t_link_access_logs " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid")
    LinkAccessStatsDO groupFindPvUvUipStatsByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    @Select("select ip,count(ip) as cnt " +
            "from t_link_access_logs " +
            "where gid=#{param.gid} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by gid,ip " +
            "order by cnt desc " +
            "limit 5 ")
    List<HashMap<String, Object>> groupListTopIpByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    @Select("select  sum(old_user) as oldUserCnt,sum(new_user) as newUserCnt " +
            " from (select  " +
            "case when count(distinct date(create_time))>1 then 1 else 0 end as old_user, " +
            "case when count(distinct date(create_time))=1 and max(create_time) >= #{param.startDate} and max(create_time)<=#{param.endDate} " +
            "then 1 else 0 end as new_user " +
            "from t_link_access_logs " +
            "where gid=#{param.gid} " +
            "GROUP BY user) " +
            "as user_counts")
    Map<String, Object> groupFindUvTypeCntByShortLink(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>"
    )
    List<Map<String, Object>> selectUvTypeByUsers(@Param("fullShortUrl") String fullShortUrl,
                                                  @Param("gid") String gid,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("userAccessLogsList") List<String> userAccessLogsList);

    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>"
    )
    List<Map<String, Object>> selectGroupUvTypeByUsers(@Param("gid") String gid,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("userAccessLogsList") List<String> userAccessLogsList);
}