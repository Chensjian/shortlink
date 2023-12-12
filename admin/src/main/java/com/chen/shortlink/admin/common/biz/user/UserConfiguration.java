package com.chen.shortlink.admin.common.biz.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class UserConfiguration {

    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter(StringRedisTemplate stringRedisTemplate){
        FilterRegistrationBean<UserTransmitFilter> registrationBean=new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserTransmitFilter(stringRedisTemplate));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }
}
