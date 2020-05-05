package com.zjx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 资源管理
 */
@Controller
@RequestMapping("/filesInfo")
public class FilesController {

    /**
     * @Description:资源管理
     * @Author: 张金鑫
     * @Date: 2020/4/18
     */
    @RequestMapping("/filesInfo")
    public String mindmap2(){
        return "view";
    }
}
