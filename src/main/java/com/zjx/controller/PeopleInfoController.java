package com.zjx.controller;

import com.zjx.model.MindMap;
import com.zjx.model.ScoringRecord;
import com.zjx.model.Token;
import com.zjx.model.UserBean;
import com.zjx.service.ExpandThinkingService;
import com.zjx.service.MindMapService;
import com.zjx.service.PeopleInfoService;
import com.zjx.util.ResponseMsg;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人信息
 */
@Controller
@RequestMapping("/perpleInfo")
public class PeopleInfoController {


    @Resource
    private  MindMapService mindMapService;
    @Resource
    private PeopleInfoService peopleInfoService;
    @Resource
    private ExpandThinkingService expandThinkingService;


    /**
     * @Description:映射新的知识图谱界面
     * @Author: 张金鑫
     * @Date: 2020/4/18
     */
    @RequestMapping("/perpleInfo")
    public String mindmap2(){
        return "stuinfo";
    }


    /**
     * 获得用户信息用户导图评分记录
     */
    @RequestMapping(value = "/getUserInfo" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String,Object> map = new HashMap<>();

        String userId = session.getAttribute("userId").toString();
        //获得用户基本信息
        UserBean userBean = peopleInfoService.getUserInfo(userId);

        map.put("name", userBean.getName());
        map.put("account", userBean.getAccount());
        map.put("cityId", userBean.getCityId());
        map.put("email", userBean.getEmail());
        map.put("phone", userBean.getPhone());

        //获得用户积分情况
        Token token = peopleInfoService.getUsetToken(userId);

        map.put("tokenGet", token.getTokenGet());//获得的总的
        map.put("tokenResidue", token.getTokenResidue());//剩余的
        map.put("tokenCunsume", token.getTokenCunsume());//消费的

        //获得用户思维导图的数量
        Integer mapNumber = mindMapService.getMapNumber(userId);

        map.put("mapNumber", mapNumber);
        return map;
    }

    /**
    * 获得用户导图评分记录
    */
    @RequestMapping(value = "/mapGradeHistory" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<ScoringRecord> mapGradeHistory(HttpServletRequest request){
        HttpSession session = request.getSession();
        String userId = session.getAttribute("userId").toString();
        List<ScoringRecord> list = expandThinkingService.getUserScoringRecord(userId);
        return list;
    }

}
