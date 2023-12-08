package com.chen.shortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.shortlink.admin.dao.entity.UserDo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户持久层
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDo> {
}
