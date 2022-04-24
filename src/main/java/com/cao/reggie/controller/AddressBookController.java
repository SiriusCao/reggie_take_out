package com.cao.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.AddressBook;
import com.cao.reggie.entity.User;
import com.cao.reggie.service.AddressBookService;
import com.cao.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list/{loginUserPhone}")
    public R<List<AddressBook>> list(@PathVariable("loginUserPhone") String phone){
        //获取当前登录用户的信息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone,phone);
        User user = userService.getOne(userWrapper);

        //根据userId查询其地址簿
        LambdaQueryWrapper<AddressBook> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId,user.getId());
        List<AddressBook> addressBookList = addressBookService.list(wrapper);

        return R.success(addressBookList);
    }

}