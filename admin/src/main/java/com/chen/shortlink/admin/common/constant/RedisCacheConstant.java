package com.chen.shortlink.admin.common.constant;

/**
 * redis缓存常量
 */
public class RedisCacheConstant {
    public static final String LOCK_USER_REGISTER_KEY="short-link:lock_user_register:";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock_group-create:%s";
}
