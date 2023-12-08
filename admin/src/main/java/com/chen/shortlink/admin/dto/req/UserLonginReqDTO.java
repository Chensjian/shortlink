package com.chen.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户登录实体
 */
@Data
public class UserLonginReqDTO {

    private String username;

    private String password;
}
