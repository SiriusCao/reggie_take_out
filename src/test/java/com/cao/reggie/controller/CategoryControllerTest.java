//package com.cao.reggie.controller;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class CategoryControllerTest {
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Test
//    public void test(){
//        ValueOperations ops = redisTemplate.opsForValue();
//        ops.set("name","cao");
//        String name = (String) ops.get("name");
//        System.out.println(name);
//        ops.set();
//
//    }
//}