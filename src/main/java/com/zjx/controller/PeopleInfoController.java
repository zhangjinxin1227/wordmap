package com.zjx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 个人信息
 */
@Controller
@RequestMapping("/perpleInfo")
public class PeopleInfoController {


    /**
     * @Description:映射新的知识图谱界面
     * @Author: 张金鑫
     * @Date: 2020/4/18
     */
    @RequestMapping("/perpleInfo")
    public String mindmap2(){
        return "stuinfo";
    }
}
