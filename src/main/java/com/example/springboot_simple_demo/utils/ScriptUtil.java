package com.example.springboot_simple_demo.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class ScriptUtil {
    public static String execCommand(String command){
        StringBuilder result = new StringBuilder();

        try {
            // String command = "cat /proc/version";
            // String command = "sh /home/cxx/Documents/test.sh";

            // 执行命令
            Process process = Runtime.getRuntime().exec(command);
            // 读取进程的输出流
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                // stringBuilder.append(System.getProperty("line.separator"));
            }

            // 等待进程执行结束
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                log.info("get command result: {}", result);

            } else {
                log.error("command failed");
            }
        } catch (Exception exception) {
            log.error("command failed: {}", exception.getMessage());
        }

        return result.toString();
    }
}
