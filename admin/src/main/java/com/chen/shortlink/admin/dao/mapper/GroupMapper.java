package com.chen.shortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.admin.dao.entity.GroupDo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短链接分组实现层
 */
@Mapper
public interface GroupMapper extends BaseMapper<GroupDo> {
}
