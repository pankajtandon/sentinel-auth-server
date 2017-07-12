package com.nayidisha.sentinel.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.nayidisha.sentinel")
public class SentinelAuthServer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SentinelAuthServer.class, args);
    }

}