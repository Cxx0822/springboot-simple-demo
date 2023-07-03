package com.example.springboot_simple_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpringBootSimpleDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSimpleDemoApplication.class, args);
    }

}
