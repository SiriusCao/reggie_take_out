package com.cao.reggie.controller;

import com.cao.reggie.common.R;
import com.cao.reggie.entity.ShoppingCart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        return null;
    }
}
