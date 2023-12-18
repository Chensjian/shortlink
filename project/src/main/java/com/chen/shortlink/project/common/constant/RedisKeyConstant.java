package com.chen.shortlink.project.common.constant;

public class RedisKeyConstant {

    /**
     * 短链接跳转前缀key
     */
    public static final String GOTO_SHORT_LINK_KEY="short-link_goto_%s";

    /**
     * 短链接空值跳转前缀 Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link_is-null_goto_%s";

    /**
     * 短链接跳转锁前缀 Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link_lock_goto_%s";

    /**
     * uv前缀
     */
    public static final String SHORT_LINK_STATS_UV="short-link:stats:uv:";

    /**
     * uip前缀
     */
    public static final String SHORT_LINK_STATS_UIP="short-link:status:uip";
}
