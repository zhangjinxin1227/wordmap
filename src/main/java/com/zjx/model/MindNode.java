package com.zjx.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MindNode implements Serializable {
    private static final long serialVersionUID = 4130163141426979745L;

    private String nodeId;//节点id

    private String nodeName;//节点名称

    private String parentId;//父节点id

    private Integer userId;//用户id

    private String createTime;//创建时间

    private String updateTime;//更新时间

    private Integer isDel;//是否删除

    private Integer color;//颜色
}
