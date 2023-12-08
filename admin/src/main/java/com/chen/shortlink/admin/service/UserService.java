package com.chen.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.admin.dao.entity.UserDo;
import com.chen.shortlink.admin.dto.req.UserLonginReqDTO;
import com.chen.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.chen.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.chen.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.chen.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDo> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 根据用户名查询用户名是否已经被使用
     * @param username 用户名
     * @return
     */
    Boolean hasUserName(String username);

    /**
     * 用户注册
     * @param userRegisterReqDTO 用户注册参数
     */
    void register(UserRegisterReqDTO userRegisterReqDTO);

    /**
     * 修改用户
     * @param userUpdateReqDTO 用户信息
     */
    void update(UserUpdateReqDTO userUpdateReqDTO);

    /**
     * 用户登录
     * @param userLonginReqDTO 用户登录参数
     */
    UserLoginRespDTO login(UserLonginReqDTO userLonginReqDTO);

    /**
     * 退出登录
     * @param token
     */
    void logout(String token);
}
