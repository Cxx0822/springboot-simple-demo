package com.example.springboot_simple_demo;

import com.example.springboot_simple_demo.entity.FtpServiceInfo;
import com.example.springboot_simple_demo.service.FtpTransferService;
import com.example.springboot_simple_demo.utils.ScriptUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@SpringBootTest
@Slf4j
class SpringBootSimpleDemoApplicationTests {
    @Test
    void contextLoads() {
        String command = "cat /proc/version";
        // String command = "sh /home/cxx/Documents/test.sh";

        String result = ScriptUtil.execCommand(command);
        System.out.println(result);
    }

}
