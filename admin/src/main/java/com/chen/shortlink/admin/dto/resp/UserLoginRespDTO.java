package com.chen.shortlink.admin.dto.resp;

import lombok.Builder;
import lombok.Data;

@Data
public class UserLoginRespDTO {
    private String token;

    private UserInfoDTO userInfo;

}
