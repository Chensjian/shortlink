package com.chen.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.chen.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.chen.shortlink.project.service.ShortLinkGotoService;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkGotoServiceImpl extends ServiceImpl<ShortLinkGotoMapper, ShortLinkGotoDO> implements ShortLinkGotoService {
}
