package com.chen.shortlink.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.swing.*;

@EnableDiscoveryClient
@SpringBootApplication
public class ShortLinkProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkProjectApplication.class,args);
    }
}
