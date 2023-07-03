package com.example.springboot_simple_demo.service.impl;

import com.example.springboot_simple_demo.entity.FtpServiceInfo;
import com.example.springboot_simple_demo.service.FtpTransferService;
import com.example.springboot_simple_demo.utils.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class FtpTransferServiceImpl implements FtpTransferService {
    @Override
    public void uploadFile(FtpServiceInfo ftpServiceInfo, String localBasePath, String localFilePath, String ftpBasePath, String ftpFilePath) {
        // 检查需要上传的文件是否存在
        File file = new File(localBasePath + File.separator + localFilePath);
        if (!file.exists()) {
            log.error("upLoadFile " + localBasePath + File.separator + localFilePath + " not exit");
            return;
        }

        String fileName = file.getName();
        log.info("start uploadFile: " + fileName);

        try {
            // 初始化Ftp服务器
            FtpUtil ftpUtil = new FtpUtil(ftpServiceInfo.hostIp, ftpServiceInfo.port, ftpServiceInfo.username, ftpServiceInfo.password);

            FileInputStream fileInputStream = new FileInputStream(file);
            if (ftpUtil.uploadFile(ftpBasePath, ftpFilePath, fileName, fileInputStream)) {
                log.info("uploadFile: " + fileName + " success");
            } else {
                log.error("uploadFile: " + fileName + " failed");
            }

            log.info("finish uploadFile");
        } catch (Exception exception) {
            log.error("uploadFile: " + fileName + " failed " + exception.getMessage());
        }
    }

    @Override
    public void downloadFile(FtpServiceInfo ftpServiceInfo, String localBasePath, String localFilePath, String ftpBasePath, String ftpFilePath) {
        log.info("start downloadFile: " + ftpFilePath);

        try {
            // 初始化Ftp服务器
            FtpUtil ftpUtil = new FtpUtil(ftpServiceInfo.hostIp, ftpServiceInfo.port, ftpServiceInfo.username, ftpServiceInfo.password);

            InputStream inputStream = null;
            inputStream = ftpUtil.downloadFile(ftpBasePath, ftpFilePath);

            File targetFile = new File(localBasePath + File.separator + localFilePath);
            FileUtils.copyInputStreamToFile(inputStream, targetFile);
            log.info("finish downloadFile");
        } catch (Exception exception) {
            log.error("downloadFile: " + ftpFilePath + " failed " + exception.getMessage());
        }
    }
}
