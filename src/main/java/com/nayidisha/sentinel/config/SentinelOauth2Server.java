package com.nayidisha.sentinel.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class SentinelOauth2Server extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SentinelOauth2Server.class, args);
    }

}