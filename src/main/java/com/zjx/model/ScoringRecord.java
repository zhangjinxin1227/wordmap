package com.zjx.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 思维导图评分记录
 */
@Data
public class ScoringRecord implements Serializable {

    private static final long serialVersionUID = 5162604968132123727L;

    private Integer recordId;//打分记录编号

    private String mapId;//图谱编号

    private Integer userId;//用户编号

    private String mapName;//思维导图名称

    private String userAccount;//用户昵称

    private Integer nodeLogic;//逻辑性分数

    private Integer nodeTotal;//知识点全面分数

    private Integer nodeArtistic;//导图美观分数

    private Integer nodeMemory;//导图记忆性分数

    private String mapRemark;//对导图的评语

    private String createTime;//创建时间

}
