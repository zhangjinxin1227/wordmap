package com.zjx.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Zsd implements Serializable {

    private static final long serialVersionUID = -6699169289543198920L;

    private String zsdId;//nodeid

    private String zsdInfo;//节点信息

    private String createTime;//创建时间

    private Integer isDel;//是否删除

    private Integer userId;//用户编号

    private String updateTime;
}
