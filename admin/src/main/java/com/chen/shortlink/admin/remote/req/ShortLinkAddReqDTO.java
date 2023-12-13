package com.chen.shortlink.admin.remote.req;

import lombok.Data;

import java.util.Date;

@Data
public class ShortLinkAddReqDTO {

    /**
     * 域名
     */
    private String domain;

    /**
     * 原始短链接
     */
    private String originUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组图标
     */
    private String favicon;

    /**
     * 创建类型 0:接口创建 1:控制台创建
     */
    private int createType;

    /**
     * 有效期类型 0:永久有效 1:自定义
     */
    private int validDateType;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 描述
     */
    private String description;
}
