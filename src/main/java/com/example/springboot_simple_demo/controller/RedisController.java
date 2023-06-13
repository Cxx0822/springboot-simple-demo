package com.example.springboot_simple_demo.controller;

import com.example.springboot_simple_demo.result.R;
import com.example.springboot_simple_demo.utils.RedisUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


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
