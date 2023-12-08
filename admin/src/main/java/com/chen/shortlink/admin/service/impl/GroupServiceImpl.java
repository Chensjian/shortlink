package com.chen.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.admin.dao.entity.GroupDo;
import com.chen.shortlink.admin.dao.mapper.GroupMapper;
import com.chen.shortlink.admin.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * 短链接分组实现层
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper,GroupDo> implements GroupService {

}
