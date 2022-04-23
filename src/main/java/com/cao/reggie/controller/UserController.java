package com.cao.reggie.controller;

import com.cao.reggie.common.R;
import com.cao.reggie.entity.User;
import com.cao.reggie.service.UserService;
import com.cao.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){
        String phone = user.getPhone();
        log.info("手机号码{}发来验证码请求", phone);
        if (StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            ValueOperations ops = redisTemplate.opsForValue();
            ops.set(phone,code,300, TimeUnit.SECONDS);
            log.info("验证码为{}",code);
            return R.success("发送成功");
        }
        return R.error("发送失败");
    }
}
