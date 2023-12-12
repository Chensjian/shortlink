package com.chen.shortlink.admin.common.enums;

import com.chen.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_NULL("B000200","用户记录不存在"),
    USER_NAME_EXIST("B000201","用户名已被使用"),
    USER_SAVE_FAIL("B000202","新增用户失败"),
    USER_UPDATE_FAIL("B000203","修改用户信息失败"),
    USER_PASSWORD_ERROR("B000204","密码错误"),
    USER_TOKEN_ERROR("B000205","token已过期，请重新登录"),

    USER_LOGIN_ERROR("B000206","用户还未登录，请先登录")
    ;

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code,String message){
        this.code=code;
        this.message=message;
    }
    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
