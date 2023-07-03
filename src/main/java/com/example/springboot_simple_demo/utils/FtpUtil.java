package com.example.springboot_simple_demo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class FtpUtil {
    private String ftpHost = "";
    private Integer ftpPort = 21;
    private String ftpUsername = "";
    private String ftpPassword = "";
    private final FTPClient ftpClient;

    /**
     * Description: 配置FTP服务器参数
     *
     * @param host     FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     */
    public FtpUtil(String host, Integer port, String username, String password) {
        ftpHost = host;
        ftpPort = port;
        ftpUsername = username;
        ftpPassword = password;
        ftpClient = new FTPClient();
    }

    /**
     * 初始化FTP客户端
     *
     * @return 是否初始化成功
     */
    private boolean initFtpClient() {
        boolean isInitFtpClient = false;
        try {
            // 连接FTP服务器
            //设置连接超时时间为10秒
            ftpClient.setConnectTimeout(5000);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftpClient.connect(ftpHost, ftpPort);
            // log.info("Ftp连接成功");

            // 登录FTP服务器
            if (!ftpClient.login(ftpUsername, ftpPassword)) {
                log.error("Ftp login failed: " + "username: " + ftpUsername + "," + "password: " + ftpPassword);
                ftpClient.disconnect();
                return false;
            }

            // log.info("Ftp登录成功");

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return false;
            }

            log.info("init Ftp success");
            isInitFtpClient = true;
        } catch (Exception exception) {
            log.error("init Ftp failed: {}", exception.getMessage());
        }

        return isInitFtpClient;
    }

    /**
     * 断开FTP连接
     */
    private void disConnectFtpClient() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (Exception exception) {
                log.error("logout failed: {}", exception.getMessage());
            }

            try {
                ftpClient.disconnect();
            } catch (Exception exception) {
                log.error("connect failed: {}", exception.getMessage());
            }
            log.info("logout success");
        }
    }

    /**
     * 切换Ftp工作目录
     *
     * @param basePath Ftp根路径
     * @param filePath Ftp文件路径
     * @return 是否切换成功
     */
    private boolean changeWorkingDirectory(String basePath, String filePath) {
        boolean isSuccess = false;

        // 切换到上传目录
        log.info("Ftp filePath: " + basePath + File.separator + filePath);
        try {
            if (!ftpClient.changeWorkingDirectory(basePath + File.separator + filePath)) {
                // 如果目录不存在创建目录
                String[] dirs = filePath.split(File.separator);
                String tempPath = basePath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) continue;
                    tempPath += File.separator + dir;

                    if (!ftpClient.changeWorkingDirectory(tempPath)) {
                        if (!ftpClient.makeDirectory(tempPath)) {
                            log.error("makeDirectory " + tempPath + " failed");
                            return false;
                        } else {
                            ftpClient.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }

            isSuccess = true;
        } catch (Exception exception) {
            log.error("changeWorkingDirectory failed: {}", exception.getMessage());
        }

        return isSuccess;
    }


    /**
     * Description: 向FTP服务器上传文件
     *
     * @param basePath FTP服务器基础目录
     * @param filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
     * @param fileName 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     */
    public boolean uploadFile(String basePath, String filePath, String fileName, InputStream input) {
        boolean isUpload = false;

        if (!initFtpClient()) {
            return false;
        }

        ftpClient.enterLocalPassiveMode();

        try {
            if (changeWorkingDirectory(basePath, filePath)) {
                log.info("start upload " + fileName);
                // 设置上传文件的类型为二进制类型
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // 上传文件
                if (!ftpClient.storeFile(fileName, input)) {
                    log.error("upload " + fileName + " failed");
                    return false;
                }
                log.info("upload " + fileName + " success");
                input.close();
                isUpload = true;
            }
        } catch (Exception exception) {
            log.error("uploadFile failed: {}", exception.getMessage());
        } finally {
            disConnectFtpClient();
        }
        return isUpload;
    }

    /**
     * Description: 向FTP服务器上传文件夹
     *
     * @param basePath   FTP服务器基础目录
     * @param filePath   FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
     * @param folderName 上传到FTP服务器上的文件夹名
     * @return 成功返回true，否则返回false
     */
    public boolean uploadDictionary(String basePath, String filePath, File folderName) {
        boolean isUpload = false;

        if (!initFtpClient()) {
            return false;
        }

        ftpClient.enterLocalPassiveMode();

        try {
            if (changeWorkingDirectory(basePath, filePath)) {
                // 列出文件夹下所有的文件
                File[] files = folderName.listFiles();
                log.info("upload file count: " + (files != null ? files.length : 0));

                // 遍历文件 上传文件
                for (int i = 0; i < (files != null ? files.length : 0); i++) {
                    FileInputStream fileInputStream = new FileInputStream(files[i]);

                    // log.info("start upload" + files[i].getName());
                    // 设置上传文件的类型为二进制类型
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    // 上传文件
                    if (!ftpClient.storeFile(files[i].getName(), fileInputStream)) {
                        log.error("upload " + files[i].getName() + " failed");
                        break;
                    }

                    fileInputStream.close();
                    // log.info("upload " + files[i].getName() + " success");
                }

                isUpload = true;
            }
        } catch (Exception exception) {
            log.error("uploadFile failed: {}", exception.getMessage());
        } finally {
            disConnectFtpClient();
        }

        return isUpload;
    }

    /**
     * 下载文件
     *
     * @param remotePath 文件路径
     * @param fileName   文件名
     * @return 输入流
     */
    public InputStream downloadFile(String remotePath, String fileName) {
        InputStream inputStream = null;

        if (!initFtpClient()) {
            return null;
        }

        ftpClient.enterLocalPassiveMode();

        try {
            // 转移到FTP服务器目录
            String ftpFilePath = remotePath + File.separator + fileName;
            log.info("Ftp filePath: " + ftpFilePath);

            // 切换目录
            ftpClient.changeWorkingDirectory(remotePath);

            FTPFile[] ftpFiles = ftpClient.listFiles(ftpFilePath);

            if (ftpFiles.length == 0) {
                log.error("Ftp is empty");
            } else {
                // 下载文件
                log.info("start download " + fileName);
                // 下载文件
                inputStream = ftpClient.retrieveFileStream(ftpFilePath);
            }
        } catch (Exception exception) {
            log.error("downloadFile failed: {}", exception.getMessage());
        } finally {
            disConnectFtpClient();
        }

        return inputStream;
    }

    /**
     * 五分钟的毫秒数
     */
    private static final long TEN_MINUTE = 5 * 60 * 1000L;

    public boolean downloadDir(String remotePath, String fileName, String localPath) {
        boolean flag = false;

        if (!initFtpClient()) {
            return false;
        }

        try {
            // 转移到FTP服务器目录
            String ftpFilePath = remotePath + File.separator + fileName;
            log.info("Ftp文件路径: " + ftpFilePath);

            FTPFile[] ftpFiles = ftpClient.listFiles();

            if (ftpFiles.length == 0) {
                log.error("Ftp文件不存在");
            } else {
                // 下载文件
                log.info("开始下载" + fileName);
                FTPFile file = ftpFiles[0];
                FileOutputStream os = null;
                long size = file.getSize();
                // 生成本地文件
                File localFile = new File(localPath + File.separator + file.getName());
                if (localFile.length() == size) {
                    log.info("文件{}已下载完成", localFile.getName());
                    return true;
                }

                // 需要断点续传
                if (localFile.exists() && localFile.isFile() && localFile.length() > 0 && localFile.length() < size) {
                    os = new FileOutputStream(localFile, true);
                } else {
                    if (!checkFile(localFile)) {
                        return false;
                    }
                    os = new FileOutputStream(localFile);
                }

                log.info("正在下载文件：{}，总大小：{}", ftpFiles[0].getName(), size);
                long start = System.currentTimeMillis();
                try {
                    InputStream inputStream = ftpClient.retrieveFileStream(file.getName());
                    try {
                        byte[] bytes = new byte[1024 * 32];
                        long step = size / 100;
                        long process = 0L;
                        long localSize = localFile.length();
                        if (localSize > size) {
                            log.error("本地文件大于服务器文件,终止下载");
                            checkFile(localFile);
                            return false;
                        }
                        if (localSize > 0) {
                            ftpClient.setRestartOffset(localSize);
                        }
                        int c;
                        if (inputStream == null) {
                            disConnectFtpClient();
                            return false;
                        }

                        while ((c = inputStream.read(bytes)) != -1) {
                            os.write(bytes, 0, c);
                            localSize += c;
                            long nowProcess = localSize / step;
                            if (size > 50000000 && nowProcess > process) { // 大于50兆才显示进度
                                process = nowProcess;
                                log.info("{}%", process);
                                if (System.currentTimeMillis() - start > TEN_MINUTE) { // 大于指定时间
                                    log.info("时间已到，未下载部分将在下次任务中下载");
                                    disConnectFtpClient();
                                    return false;
                                }
                            }
                        }
                        log.info("文件下载完成到：{}", localPath + "/" + file.getName());
                    } catch (SocketTimeoutException e) {
                        log.info("下载出错：", e);
                        return false;
                    } catch (Exception e) {
                        log.error("下载出错：", e);
                        return false;
                    } finally {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            log.debug("关闭流错误", e); // 可以忽略的错误
                        }
                    }
                } catch (Exception e) {
                    log.error("下载文件错误：{}", file.getName(), e);
                    return false;
                } finally {
                    try {
                        os.close();
                    } catch (Exception e) {
                        log.debug("关闭连接异常"); // 可以忽略的错误类型
                    }
                }
            }
            flag = true;
        } catch (Exception exception) {
            log.error("下载文件错误：" + exception.getMessage());
            return false;
        }
        return flag;
    }

    private boolean checkFile(File localFile) {
        if (localFile.exists()) { // 已经存在则删除
            localFile.delete();
        }
        // 然后再创建
        localFile.mkdirs();
        if (localFile.exists()) {
            localFile.delete();
        }
        try {
            localFile.createNewFile();
            return true;
        } catch (IOException e) {
            log.error("创建文件失败", e);
            return false;
        }
    }

    /**
     * 输入文件流及文件名称返回给浏览器下载
     *
     * @param response    浏览器响应对象
     * @param inputStream 文件流
     * @param fileName    文件名称
     */
    public static void fileInputStreamToResponse(HttpServletResponse response, InputStream inputStream, String fileName) {
        try {
            //  清空response
            response.reset();
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存表示以附件方式下载
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            //告知浏览器文件的大小
            response.addHeader("Content-Length", "" + inputStream.available());
            response.addHeader("Access-Control-Allow-Origin", "*");
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buffer = new byte[2048];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ioException) {
            log.error("fileInputStreamToResponse: " + ioException);
        }
    }

    public static void fileToZip(String filePath, ZipOutputStream zipOut) throws IOException {
        // 需要压缩的文件
        File file = new File(filePath);
        // 获取文件名称,如果有特殊命名需求,可以将参数列表拓展,传fileName
        String fileName = file.getName();
        FileInputStream fileInput = new FileInputStream(filePath);
        // 缓冲
        byte[] bufferArea = new byte[1024 * 10];
        BufferedInputStream bufferStream = new BufferedInputStream(fileInput, 1024 * 10);
        // 将当前文件作为一个zip实体写入压缩流,fileName代表压缩文件中的文件名称
        zipOut.putNextEntry(new ZipEntry(fileName));
        int length = 0;
        // 最常规IO操作,不必紧张
        while ((length = bufferStream.read(bufferArea, 0, 1024 * 10)) != -1) {
            zipOut.write(bufferArea, 0, length);
        }
        //关闭流
        fileInput.close();
        // 需要注意的是缓冲流必须要关闭流,否则输出无效
        bufferStream.close();
        // 压缩流不必关闭,使用完后再关
    }
}
