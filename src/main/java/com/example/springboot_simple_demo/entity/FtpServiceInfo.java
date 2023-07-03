package com.example.springboot_simple_demo.entity;

import lombok.Data;

@Data
public class FtpServiceInfo {
    // Ip
    public String hostIp;
    // 端口号
    public Integer port;
    // 用户名
    public String username;
    // 密码
    public String password;

    public FtpServiceInfo (String host, Integer port, String username, String password){
        this.hostIp = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
