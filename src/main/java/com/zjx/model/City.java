package com.zjx.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class City implements Serializable {

    private static final long serialVersionUID = 7356820754927405638L;

    private Integer cityId;//城市编号

    private String cityName;//城市名称
}
