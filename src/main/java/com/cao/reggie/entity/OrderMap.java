package com.cao.reggie.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderMap implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;

    private Integer pageSize;

    private String beginTime;

    private String endTime;

    private String number;

}
