package com.chen.shortlink.project.common.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum VailDateTypeEnum {

    PERMANENT(0),
    CUSTOM(1);

    private final int type;

    public int getType() {
        return type;
    }
}
