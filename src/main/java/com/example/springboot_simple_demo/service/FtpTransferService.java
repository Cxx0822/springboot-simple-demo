package com.example.springboot_simple_demo.service;

import com.example.springboot_simple_demo.entity.FtpServiceInfo;

public interface FtpTransferService {
    /**
     * 上传单个文件
     * @param ftpServiceInfo FTP服务器信息
     * @param localBasePath 本地文件根路径
     * @param localFilePath 本地文件路径
     * @param ftpBasePath FTP文件根路径
     * @param ftpFilePath FTP文件路径
     */
    void uploadFile(FtpServiceInfo ftpServiceInfo,
                      String localBasePath, String localFilePath,
                      String ftpBasePath, String ftpFilePath);

    /**
     * 下载个文件
     * @param ftpServiceInfo FTP服务器信息
     * @param localBasePath 本地文件根路径
     * @param localFilePath 本地文件路径
     * @param ftpBasePath FTP文件根路径
     * @param ftpFilePath FTP文件路径
     */
    void downloadFile(FtpServiceInfo ftpServiceInfo,
                    String localBasePath, String localFilePath,
                    String ftpBasePath, String ftpFilePath);
}
