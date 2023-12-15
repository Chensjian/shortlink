package com.chen.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.chen.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.chen.shortlink.project.service.LinkAccessStatsService;
import org.springframework.stereotype.Service;

/**
 * 短链接访问监控接口实现层
 */
@Service
public class LinkAccessStatsServiceImpl extends ServiceImpl<LinkAccessStatsMapper, LinkAccessStatsDO> implements LinkAccessStatsService {

}
