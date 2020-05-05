package com.zjx.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MindMap implements Serializable {

    private static final long serialVersionUID = -5254842606535837516L;

    private String mapId;//导图编号

    private Integer userId;//用户编号

    private String createTime;//创建时间

    private String rootName;//导图名称

    private String userName;//用户姓名

    private String data;//导图数据

    private String mapList;//导图数据

    private String updateTime;//更新时间

    private Integer isDel;//是否删除

    private Integer isShare;//是否分享

    private String shareTime;//分享时间

    private Integer integral;//导图的平均评分 即 总分数

    private Integer logicGrade;//导图逻辑性综合评分

    private Integer totalGrade;//知识点全面性综合评分

    private Integer artisticGrade;//美观度综合评分

    private Integer memoryGrade;//可记忆性评分
}
