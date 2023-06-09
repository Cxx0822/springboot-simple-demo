Spring Boot项目Demo
# 项目概述
&emsp;&emsp;本项目为Spring Boot中常用的一些技术Demo。

# 技术栈
&emsp;&emsp;Spring Boot 2.5.9

# 配置跨越
## 跨域
&emsp;&emsp;浏览器为了页面安全，设置了同源策略：即本域脚本只能读写本域内的资源，而无法访问其它域的资源。所谓同源就是“协议+域名+端口”三者相同，当在一个站点内访问非该同源的资源，浏览器就会报跨域错误。     
&emsp;&emsp;[SpringBoot实战-跨域问题原理及解决](https://zhuanlan.zhihu.com/p/354989118)    

## 配置Maven：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 重写WebMvcConfigurer的addCorsMappings方法：
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有接口都支持跨域
        registry.addMapping("/**")
                // 允许所有地址都可以访问
                .allowedOrigins("*")
                // 允许全部原始头信息
                .addAllowedHeader("*")
                //允许所有请求方法跨域调用
                .allowedMethods("*");
    }
}
```

## 参考资料：[SpringBoot 项目解决跨域的几种方案](https://juejin.cn/post/7229139006080253989)   

# 映射本地文件到URL路径
## 配置Maven：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 重写WebMvcConfigurer的addResourceHandlers方法：
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 本地路径 要带上末尾的 "/"
    private static final String locationPath = "/**/";
    // 映射路径 例http://Ip:Port/downloads/***.***
    private static final String webPath = "/downloads/**";

    // 将本地文件映射到Url 可以直接下载
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(webPath).addResourceLocations("file:" + locationPath);
    }
}
```

# Redis
&emsp;&emsp;Redis是开放源代码（BSD许可）的内存中数据结构存储，用作数据库，缓存和消息代理。
## Redis安装
&emsp;&emsp;[Redis的安装教程](https://blog.csdn.net/weixin_43883917/article/details/114632709)  

## 启动Redis
&emsp;&emsp;cmd进入安装目录，输入命令: 
```cmd
redis-server.exe
```

## 配置Maven
```xml
<!--redis（spring-boot-starter-data-redis中包含的Lettuce）-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 整合Redis(Lettuce方式)
### yml配置
```yml
spring:
  redis:
    # Redis本地服务器地址，注意要开启redis服务，即那个redis-server.exe
    host: 127.0.0.1
    # Redis服务器端口,默认为6379.若有改动按改动后的来
    port: 6379
    #Redis服务器连接密码，默认为空，若有设置按设置的来
    password:
    jedis:
      pool:
        # 连接池最大连接数，若为负数则表示没有任何限制
        max-active: 8
        # 连接池最大阻塞等待时间，若为负数则表示没有任何限制 单位ms
        max-wait: 3000
        # 连接池中的最大空闲连接
        max-idle: 8

```

### config配置
```java
@Configuration
public class LettuceRedisConfig {
    // 自定义的redisTemplate
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        // 创建一个RedisTemplate对象，为了方便返回key为string，value为Serializable
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        // key采用string的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // value采用jackson的序列化方式
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}
```

### Redis工具类
&emsp;&emsp;[RedisUtil.java](./src/main/java/com/example/springboot_simple_demo/utils/RedisUtil.java)

&emsp;&emsp;参考资料：[自定义RedisTemplate和工具类](https://juejin.cn/post/7031418915515269127)    
&emsp;&emsp;参考资料：[springboot项目中redis客户端](https://blog.csdn.net/Ye_GuoLin/article/details/115208061)

## 简单使用
```java
@RestController
@RequestMapping("/redis")
public class RedisController {
    @Resource
    private RedisUtil redisUtil;

    /**
     * 生成Redis数据
     * @param redisId 序号
     * @return Result
     */
    @GetMapping("/generateRedis")
    public R generateCaptcha(@RequestParam String redisId) {
        String redisValue = "";
        // 判断Redis的键是否存在
        if (redisUtil.exists(redisId)) {
            // 存在则返回该值
            redisValue = redisUtil.get(redisId);
        } else {
            // 不存在则重新生成 (根据业务需求自定义Redis值)
            redisValue = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            redisUtil.set(redisId, redisValue, 60, TimeUnit.SECONDS);
        }

        return R.ok().data("redisValue", redisValue);
    }

    /**
     * 获取Redis数据
     * @param redisId 序号
     * @return Result
     */
    @GetMapping("/getRedis")
    public R getRedis(@RequestParam String redisId) {
        // 判断Redis的键是否存在
        if (redisUtil.exists(redisId)) {
            // 存在则返回该值
            String redisValue = redisUtil.get(redisId);
            return R.ok().data("redisValue", redisValue);
        } else {
            return R.ok().data("redisValue", "验证码已失效");
        }
    }

    /**
     * 校验Redis数据
     * @param redisId 序号
     * @param redisValue Redis值
     * @return Result
     */
    @PostMapping("/checkRedis")
    public R checkRedis(@RequestParam String redisId,
                          @RequestParam String redisValue) {

        if (redisUtil.get(redisId).equals(redisValue)) {
            return R.ok().message("success");
        } else {
            return R.error().message("failed");
        }
    }
}
```

# Feign调用远程接口
## Feign原理
参考资料:[SpringBoot项目中使用feign调用远程http接口](https://blog.csdn.net/weixin_40861707/article/details/124209355)

## 配置Maven
```xml
<!-- Feign 注意和Springboot的版本！ -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>3.0.6</version>
</dependency>
```

## 启动类增加注释
```java
@SpringBootApplication
@EnableFeignClients
public class SpringbootSimpleDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootSimpleDemoApplication.class, args);
    }

}
```

## 简单使用
```java
@FeignClient(name = "feignClient", url = "http://192.168.1.1:8080")
public interface FeignClientService {
    @PostMapping("/feignClient/testFeignClient")
    R testFeignClient(@RequestParam String name);
}
```

&emsp;&emsp;url为远程服务器的地址，`@PostMapping`为调用远程Post接口，括号内参数为具体的url路径。

# 自启动配置
## 继承CommandLineRunner
```java
@Component
@Slf4j
public class CommandLineRunnerImpl implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        log.info("Hello World");
    }
}
```

# FTP传输文件
## FTP协议
&emsp;&emsp;FTP(File Transfer Protocol)：是应用层的一个文件传输协议。主要作用是在服务器和客户端之间实现文件的传输和共享。FTP协议运行在TCP连接上，保证了文件传输的可靠性。      
&emsp;&emsp;参考资料:[【FTP协议】概述篇](https://zhuanlan.zhihu.com/p/337513218)

## 服务器安装ftp
1. 下载vsftpd：sudo apt-get install vsftpd       
2. 打开配置文件/ect/vsftpd.conf，将write_enable=YES注释取消   

## FTP工具类
&emsp;&emsp;[FtpUtil.java](./src//main//java//com//example//springboot_simple_demo/utils/FtpUtil.java)

## 简单使用
&emsp;&emsp;`FtpTransferService.java`
```java
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
```

&emsp;&emsp;`FtpTransferServiceImpl.java`
```java
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
```

&emsp;&emsp;`SpringBootSimpleDemoApplicationTests.java`
```java
@Test
void TestFtp() {
    FtpServiceInfo ftpServiceInfo = new FtpServiceInfo("127.0.0.1", 21, "cxx", " ");
    ftpTransferService.uploadFile(ftpServiceInfo,
            "/home/cxx/Documents", "test.txt",
            "/home/cxx/Downloads", "");

    // ftpTransferService.downloadFile(ftpServiceInfo,
    //         "/home/cxx/Documents", "test.txt",
    //         "/home/cxx/Downloads", "test.txt");
}
```

# 调用脚本
## 简单使用
```java
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
```