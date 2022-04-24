package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.reggie.common.CustomException;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.User;
import com.cao.reggie.service.UserService;
import com.cao.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        //获取手机号
        String phone = user.getPhone();
        log.info("手机号码{}发来验证码请求", phone);
        if (StringUtils.isNotEmpty(phone)) {
            //生成刘伟验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();

            // TODO: 2022/4/23 使用短信服务 发送短信验证码给客户，没钱买服务器就不做了

            //将验证码存入redis，有效期为300秒
            ValueOperations ops = redisTemplate.opsForValue();
            ops.set(phone, code, 300, TimeUnit.SECONDS);
            log.info("验证码为{}", code);
            return R.success("发送成功");
        }
        return R.error("发送失败");
    }

    /**
     * 验证短信验证码完成登录操作
     *
     * @param phoneAndCode 存储手机号码和对应的短信验证码
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> phoneAndCode, HttpServletRequest request) {
        //获取传回的手机号和验证码
        String phone = phoneAndCode.get("phone");
        String code = phoneAndCode.get("code");

        //获取存储在redis当中的短信验证码，进行比对
        ValueOperations ops = redisTemplate.opsForValue();
        String codeInRedis = (String) ops.get(phone);
        //如果为空则说明验证码已经失效或者手机号码错误
        if (codeInRedis == null) {
            throw new CustomException("验证码已经失效或者手机号码错误");
        }
        //比对是否成功
        if (code.equals(codeInRedis)) {
            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册后自动登录
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, phone);
            User user = userService.getOne(wrapper);
            if (user == null) {
                //自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                user.setName("用户"+ UUID.randomUUID().toString().substring(0,8));
                userService.save(user);
            }
            //登录
            request.getSession().setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("验证码错误");
    }
}
