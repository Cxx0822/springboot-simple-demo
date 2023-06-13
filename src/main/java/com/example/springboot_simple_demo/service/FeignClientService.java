package com.example.springboot_simple_demo.service;


import com.example.springboot_simple_demo.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "feignClient", url = "http://192.168.1.1:8080")
public interface FeignClientService {
    @PostMapping("/feignClient/testFeignClient")
    R testFeignClient(@RequestParam String name);
}
