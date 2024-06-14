package com.chen.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.shortlink.project.dao.entity.ShortLinkDo;
import com.chen.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLinkDo> {
    /**
     *
     */
    @Update("update t_link " +
            "set total_pv=total_pv+#{totalPv}, " +
            "total_uv=total_uv+#{totalUv}, " +
            "total_uip=total_uip+#{totalUip} " +
            "where full_short_url=#{fullShortUrl} " +
            "and gid=#{gid} ")
    void incrementStats(@Param("gid") String gid,
                        @Param("fullShortUrl") String fullShortUrl,
                        @Param("totalPv") int totalPv,
                        @Param("totalUv") int totalUv,
                        @Param("totalUip") int totalUip);

    /**
     * 分页统计短链接
     */
    IPage<ShortLinkDo> pageLink(ShortLinkPageReqDTO shortLinkPageReqDTO);
}
