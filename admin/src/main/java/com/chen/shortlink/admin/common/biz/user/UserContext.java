package com.chen.shortlink.admin.common.biz.user;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.chen.shortlink.admin.dto.resp.UserInfoDTO;

import java.util.Optional;

public class UserContext {
    private static final ThreadLocal<UserInfoDTO> USER_TREAD_LOCAL=new TransmittableThreadLocal<>();


    /**
     * 设置用户上下文
     * @param user
     */
    public static void setUser(UserInfoDTO user){
        USER_TREAD_LOCAL.set(user);
    }

    /**
     * 获取上下文中用户id
     * @return
     */
    public static String getUserId(){
        UserInfoDTO userInfoDTO = USER_TREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserId).orElse(null);
    }

    /**
     * 获取上下文中用户名
     * @return
     */
    public static String getUsername(){
        UserInfoDTO userInfoDTO=USER_TREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUsername).orElse(null);
    }

    public static void remove(){
        USER_TREAD_LOCAL.remove();
    }

}
