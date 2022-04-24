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
import java.util.stream.Collectors;

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
        //如果是还未添加过地址的新用户，则把添加的第一个地址设为默认地址
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId,currentId);
        int count = addressBookService.count(wrapper);
        if (count==0){
            addressBook.setIsDefault(1);
        }
        addressBook.setUserId(currentId);
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

    @PutMapping("/default")
    public R<String> updateDefault(@RequestBody AddressBook addressBook){
        //将所有默认地址取消
        LambdaQueryWrapper<AddressBook> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(wrapper);
        list = list.stream().map(item -> {
            item.setIsDefault(0);
            return item;
        }).collect(Collectors.toList());
        addressBookService.updateBatchById(list);

        //更改默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success("修改成功");
    }

    @GetMapping("/default")
    public R<AddressBook> getdefault(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId,currentId).eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = addressBookService.getOne(wrapper);
        if (addressBook!=null){
            return R.success(addressBook);
        }
        return R.error("没有找到该对象");
    }

}
