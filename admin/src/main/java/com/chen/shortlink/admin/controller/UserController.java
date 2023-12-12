package com.chen.shortlink.admin.controller;

import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.common.convention.result.Results;
import com.chen.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.chen.shortlink.admin.dto.req.UserLonginReqDTO;
import com.chen.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.chen.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.chen.shortlink.admin.dto.resp.UserActualDTO;
import com.chen.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.chen.shortlink.admin.dto.resp.UserRespDTO;
import com.chen.shortlink.admin.service.UserService;
import com.chen.shortlink.admin.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    /**
     * 根据用户名获取脱敏的用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result getUserByUsername(@PathVariable String username){
        UserRespDTO user=userService.getUserByUsername(username);
        return Results.success(user);
    }

    /**
     * 根据用户名获取无脱敏的用户信息
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result getActualUserByUsername(@PathVariable String username){
        UserRespDTO user=userService.getUserByUsername(username);
        return Results.success(BeanUtil.convert(user, UserActualDTO.class));
    }

    /**
     * 根据用户名获取无脱敏的用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result hasUserName(@RequestParam("username") String username){
        return Results.success(userService.hasUserName(username));
    }

    /**
     * 用户注册
     * @param userRegisterReqDTO 用户注册参数
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result register(@RequestBody UserRegisterReqDTO userRegisterReqDTO){
        userService.register(userRegisterReqDTO);
        return Results.success();
    }

    /**
     * 修改用户
     * @param userUpdateReqDTO 用户注册参数
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/user")
    public Result update(@RequestBody UserUpdateReqDTO userUpdateReqDTO){
        userService.update(userUpdateReqDTO);
        return Results.success();
    }

    /**
     * 用户登录
     * @param userLonginReqDTO 用户登录参数
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLonginReqDTO userLonginReqDTO){
        return Results.success(userService.login(userLonginReqDTO));
    }

    /**
     * 退出登录
     * @param token
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user/logout")
    public Result logout(@RequestParam(required = false) String token){
        userService.logout(token);
        return Results.success();
    }
}
