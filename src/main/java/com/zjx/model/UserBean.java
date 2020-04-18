package com.zjx.model;


import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
public class UserBean implements Serializable {

    private static final long serialVersionUID = 2513959756853027289L;

    private Integer id;//主键

    private String account;//账号

    private String name;//姓名

    private String password;//密码

    private String email;//邮箱

    private String phone;//手机号

    private Integer isDel;//是否删除，1删除 0未删除

    private Integer cityId;//城市id

    private String address;

    private String createTime;//创建时间
}
