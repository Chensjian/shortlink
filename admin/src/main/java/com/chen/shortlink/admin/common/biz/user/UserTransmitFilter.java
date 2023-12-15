package com.chen.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chen.shortlink.admin.common.constant.UserConstant;
import com.chen.shortlink.admin.common.convention.exception.ClientException;
import com.chen.shortlink.admin.common.convention.result.Results;
import com.chen.shortlink.admin.dto.resp.UserInfoDTO;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static com.chen.shortlink.admin.common.enums.UserErrorCodeEnum.USER_LOGIN_ERROR;
import static com.chen.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_ERROR;

@Data
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> IGNORE_URI= Lists.newArrayList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/actual/user/has-username",
            "/api/short-link/admin/v1/title"
    );
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();


        if(!IGNORE_URI.contains(requestURI)){
            String method = httpServletRequest.getMethod();
            if (!(Objects.equals(requestURI, "/api/short-link/admin/v1/user") && Objects.equals(method, "POST"))) {
                String token = httpServletRequest.getHeader(UserConstant.USER_TOKEN_KEY);
                if (!StrUtil.isAllNotBlank(token)) {
                    returnJson((HttpServletResponse) servletResponse, JSONUtil.toJsonStr(Results.failure(new ClientException(USER_LOGIN_ERROR))));
                    return;
                }
                UserInfoDTO userInfoDTO;
                String userInfoJsonStr=null;
                try {
                    userInfoJsonStr = stringRedisTemplate.opsForValue().get(token);
                    if (userInfoJsonStr == null) {
                        throw new ClientException(USER_LOGIN_ERROR);
                    }
                } catch (Exception ex) {
                    returnJson((HttpServletResponse) servletResponse, JSONUtil.toJsonStr(Results.failure(new ClientException(USER_LOGIN_ERROR))));
                    return;
                }
                userInfoDTO = JSONUtil.toBean(userInfoJsonStr, UserInfoDTO.class);
                UserContext.setUser(userInfoDTO);
            }
        }

        try {
            filterChain.doFilter(servletRequest,servletResponse);
        }finally {
            UserContext.remove();
        }
    }
    private void returnJson(HttpServletResponse response, String json) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }
}
