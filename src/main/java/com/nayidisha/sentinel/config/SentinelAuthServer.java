package com.nayidisha.sentinel.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class SentinelAuthServer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SentinelAuthServer.class, args);
    }

}