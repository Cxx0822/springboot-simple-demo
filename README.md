Spring Boot项目Demo
# 项目概述
&emsp;&emsp;本项目为Spring Boot中常用的一些技术Demo。

# 技术栈
&emsp;&emsp;Spring Boot 2.7.13

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
                // 允许跨越发送cookie
                .allowCredentials(true)
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
&emsp;&emsp;参考资料：[自定义RedisTemplate和工具类](https://juejin.cn/post/7031418915515269127)    
&emsp;&emsp;参考资料：[springboot项目中redis客户端](https://blog.csdn.net/Ye_GuoLin/article/details/115208061)

## 简单使用

# Feign调用远程接口
## Feign原理

参考资料:[SpringBoot项目中使用feign调用远程http接口](https://blog.csdn.net/weixin_40861707/article/details/124209355)