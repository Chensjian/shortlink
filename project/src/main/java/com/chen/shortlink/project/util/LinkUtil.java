package com.chen.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.chen.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 短链接获取缓存有效时间
     * @param validate 有效期时间
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidTime(Date validate){
        return Optional.ofNullable(validate)
                .map(each-> DateUtil.between(new Date(),each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
}
