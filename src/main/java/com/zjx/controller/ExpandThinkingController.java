package com.zjx.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjx.model.MindMap;
import com.zjx.model.ScoringRecord;
import com.zjx.service.ExpandThinkingService;
import com.zjx.service.MindMapService;
import com.zjx.util.ResponseMsg;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Response;
import java.util.List;
import java.util.Map;

/**
 * 思维拓展
 */
@Controller
@RequestMapping("/expandThinking")
public class ExpandThinkingController {

    @Resource
    private ExpandThinkingService expandThinkingService;
    @Resource
    private MindMapService mindMapService;

    /**
     * @Description:映射新的知识图谱界面
     * @Author: 张金鑫
     * @Date: 2020/4/18
     */
    @RequestMapping("/expandThinking")
    public String mindmap2(){
        return "study";
    }


    /**
     * @Description:获得所有分享的思维导图列表
     * @Author: 张金鑫
     * @Date: 2020/4/18
     */
    @RequestMapping(value = "/geShareAllMap" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseMsg geShareAllMap(@RequestBody Map<String, String> map){

        String pageNum = map.get("pageNum");
        String pageSize = map.get("pageSize");
        Integer pageNumInt;
        Integer pageSizeInt;

        //判断分页页码，及长度
        if(pageNum == null || pageNum.equals("")){
            pageNumInt = 1;
        } else {
            pageNumInt = Integer.parseInt(pageNum);
        }
        if(pageSize == null || pageSize.equals("")){
            pageSizeInt = 10;
        } else {
            pageSizeInt = Integer.parseInt(pageSize);
        }
        PageHelper.startPage(pageNumInt,pageSizeInt);
        List<MindMap> list = expandThinkingService.getShareMapList();
        PageInfo<MindMap> pageInfo = new PageInfo<>(list);
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setCode(200);
        responseMsg.setMsg("成功");
        responseMsg.setData(pageInfo);
        return responseMsg;
    }


    /**
     * 查看评论列表
     */
    @RequestMapping(value = "/seeComment" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<ScoringRecord> seeComment(@RequestBody String rootId){
        return expandThinkingService.getSeeComment(rootId);
    }

    /**
     * 获得思维导图的综合评分
     */
    @RequestMapping(value = "/getMapGrade" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public MindMap getMapGrade(@RequestBody String rootId){
        return expandThinkingService.getMapGrade(rootId);
    }


    /**
     * 获得搜索思维导图的列表
     */
    @RequestMapping(value = "/getSearchMindByName" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseMsg getSearchMindByName(@RequestBody Map<String, String> map){

        String pageNum = map.get("pageNum");
        String pageSize = map.get("pageSize");
        String searchMessage = map.get("searchMessage");
        Integer pageNumInt;
        Integer pageSizeInt;

        //判断分页页码，及长度
        if(pageNum == null || pageNum.equals("")){
            pageNumInt = 1;
        } else {
            pageNumInt = Integer.parseInt(pageNum);
        }
        if(pageSize == null || pageSize.equals("")){
            pageSizeInt = 10;
        } else {
            pageSizeInt = Integer.parseInt(pageSize);
        }
        PageHelper.startPage(pageNumInt,pageSizeInt);
        List<MindMap> list = expandThinkingService.getSearchMindByName(searchMessage);
        PageInfo<MindMap> pageInfo = new PageInfo<>(list);
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setCode(200);
        responseMsg.setMsg("成功");
        responseMsg.setData(pageInfo);
        return responseMsg;
    }

    /**
     * 用户为思维导图打分接口
     */
    @RequestMapping(value = "/userMark" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseMsg userMark(@RequestBody Map<String, String> map, HttpServletRequest request){
        ResponseMsg responseMsg = new ResponseMsg();

        HttpSession session = request.getSession();
        String userId = session.getAttribute("userId").toString();

        String rootId = map.get("rootId");
        MindMap mindMap = mindMapService.getMindMap(rootId);
        String mindUser = mindMap.getUserId().toString();
        //防止自己为自己的知识图谱打分
        if (!userId.equals(mindUser)){
            responseMsg.setCode(300);
            responseMsg.setMsg("不可以为自己的导图打分");
            return responseMsg;
        }

        ScoringRecord scoringRecord = new ScoringRecord();
        scoringRecord.setMapId(rootId);
        scoringRecord.setUserId(Integer.parseInt(userId));
        scoringRecord.setNodeArtistic(Integer.parseInt(map.get("nodeArtistic")));
        scoringRecord.setNodeLogic(Integer.parseInt(map.get("nodeLogic")));
        scoringRecord.setNodeMemory(Integer.parseInt(map.get("nodeMemory")));
        scoringRecord.setNodeTotal(Integer.parseInt(map.get("nodeTotal")));
        scoringRecord.setMapRemark(map.get("mapRemark"));

        Integer i = expandThinkingService.insertMark(scoringRecord);
        if(i > 0){
            responseMsg.setCode(200);
            responseMsg.setMsg("打分成功");
            return responseMsg;
        } else {
            responseMsg.setCode(400);
            responseMsg.setMsg("打分失败");
            return responseMsg;
        }

    }




}
