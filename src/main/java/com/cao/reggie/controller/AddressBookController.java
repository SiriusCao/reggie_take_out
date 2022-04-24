package com.cao.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.reggie.common.BaseContext;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.AddressBook;
import com.cao.reggie.entity.User;
import com.cao.reggie.service.AddressBookService;
import com.cao.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public R<String> add(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        log.info("新增{}",addressBook.toString());
        return R.success("新增成功");
    }

    @GetMapping("/{id}")
    public R<AddressBook> listById(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook!=null) {
            return R.success(addressBook);
        }
        return R.error("操作失败");
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        boolean b = addressBookService.updateById(addressBook);
        if (b){
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    @DeleteMapping()
    public R<String> delete(Long id){
        boolean b = addressBookService.removeById(id);
        if (b){
            log.info("删除{}",id);
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

}
