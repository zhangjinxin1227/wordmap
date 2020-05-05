package com.zjx.model;

import lombok.Data;

@Data
public class Token {

    private Integer tokenId;//token编号

    private Integer userId;//用户编号

    private Integer tokenGet;//用户获得的总积分数

    private Integer tokenResidue;//用户剩余的积分数

    private Integer tokenCunsume;//用户消费的积分数

    private String updateTime;//记录更新时间
}
