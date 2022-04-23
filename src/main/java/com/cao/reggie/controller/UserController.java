package com.cao.reggie.controller;

import com.cao.reggie.common.R;
import com.cao.reggie.entity.User;
import com.cao.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){
        log.info("手机号码{}发来验证码请求",user.getPhone());
        return R.success("发送成功");
    }
}
