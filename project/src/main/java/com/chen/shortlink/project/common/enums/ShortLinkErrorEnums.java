package com.chen.shortlink.project.common.enums;

import com.chen.shortlink.project.common.convention.errorcode.IErrorCode;

public enum ShortLinkErrorEnums implements IErrorCode {

    SHORT_ADD_ERROR("C000200","短链接频繁创建，请稍后再试"),
    SHORT_ADD_REPEAT_ERROR("C000201","短链接生成重复")
    ;

    private final String code;

    private final String message;

    ShortLinkErrorEnums(String code,String message){
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
