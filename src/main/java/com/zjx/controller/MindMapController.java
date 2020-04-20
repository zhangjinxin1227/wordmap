package com.zjx.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjx.json.FileUpload;
import com.zjx.json.JsonAnalyze;
import com.zjx.json.StreamToBlob;
import com.zjx.model.MindMap;
import com.zjx.model.MindNode;
import com.zjx.model.UploadFile;
import com.zjx.service.FileService;
import com.zjx.service.MindMapService;
import com.zjx.service.MindNodeService;
import com.zjx.service.impl.StatusMap;
import com.zjx.util.ResponseMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/mindMap")
public class MindMapController {

    @Autowired
    private FileUpload fileUpload;
    @Autowired
    private StreamToBlob streamToBlob;
    @Autowired
    private JsonAnalyze jsonAnalyze;
    @Autowired
    private StatusMap statusMap;
    @Autowired
    private MindMapService mindMapService;
    @Autowired
    private FileService fileService;
    @Autowired
    private MindNodeService mindNodeService;

    private static final Logger logger = LoggerFactory.getLogger(MindMapController.class);

    /**
    * @Description:映射新的知识图谱界面
    * @Author: 张金鑫
    * @Date: 2020/4/18
    */
    @RequestMapping("/mindmap")
    public String mindmap2(){
        return "mindmap";
    }

    /**
     * @author 张金鑫
     * @Date 2020-4-19
     * 映射各个节点的笔记页面(新版)
     */
    @RequestMapping("/summer")
    public String summer(){
        return "summer";
    }

    /**
     * @author 张金鑫
     * 映射更新数据（change）
     * @return
     */
    @RequestMapping("/dataChange")
    public String xxml(){
        return "dataChange";
    }

    /**
     * 张金鑫
     * 新建知识图谱
     * @param nodeName
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("newNewMind")
    @ResponseBody
    public String newNewMind(@RequestParam String nodeName, HttpServletRequest request) throws IOException{

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> map2 = new HashMap<String, Object>();
        Map<String, Object> map3 = new HashMap<String, Object>();
        Map<String, Object> map4 = new HashMap<String, Object>();

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String id = df.format(new Date());
        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("userId"));

        if (userid.equals(null)||userid.equals("null")){
            return statusMap.a("2");
        }

        map.put("id", id);
        map.put("topic", nodeName);
        map2.put("author", "hizzgdev@163.com");
        map2.put("name", "jsMindremote");
        map2.put("version", "0.2");
        map3.put("data", map);
        map3.put("meta", map2);
        map3.put("format", "node_tree");


        String datas = jsonAnalyze.object2Json(map3).toString();
        datas = datas.replace("\"", "'");
        datas = datas.replace(" ", "");
        datas = datas.replace("'{", "{");
        datas = datas.replace("}'", "}");

        map4.put("datas", datas);
        map4.put("kcmc", id);
        map4.put("mindJson2", "success");
        map4.put("state", Integer.valueOf(1));

        String data = jsonAnalyze.map2Json(map4);
        //获得用户真实姓名
        String realName = session.getAttribute("userName").toString();
        //用户id
        Integer userId = Integer.parseInt(session.getAttribute("userId").toString());

        //存节点数据
        MindNode mindNode = new MindNode();
        mindNode.setNodeId(id);
        mindNode.setNodeName(nodeName);
        mindNode.setParentId("00100");

        List<MindNode> list = new ArrayList<MindNode>();
        list.add(mindNode);
        String activeData = jsonAnalyze.list2Json(list);

        //存数据库
        MindMap mindmap = new MindMap();
        mindmap.setMapId(id);
        mindmap.setData(data);
        mindmap.setRootName(nodeName);
        mindmap.setUserId(userId);
        mindmap.setMapList(activeData);

        if(mindMapService.saveMindMap(mindmap)){
            return data;
        }
        return statusMap.a("2");

    }



    /**
     * @author 张金鑫
     * 新建子节
     * @param mindNode
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("/saveMapNode")
    @ResponseBody
    public String saveMapNode(MindNode mindNode,
                              HttpServletRequest request) throws IOException{

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        //新建的节点id用时间表示
        String nodeId = df.format(new Date());
        String nodeName = mindNode.getNodeName();
        String parentId = mindNode.getParentId();
        String rootId = mindNode.getRootId();

        HttpSession session = request.getSession();
        //获得当前用户id
        String userId = String.valueOf(session.getAttribute("userId"));

        if (userId == null || userId.equals("")){
            return statusMap.a("2");
        }

        //根据根节点id获得思维导图
        MindMap mindMap = mindMapService.getMindMap(rootId);
        String activeList = mindMap.getMapList();
        String mindUser = mindMap.getUserId().toString();
        //防止其他人修改自己的知识图谱
        if (!userId.equals(mindUser)){
            return statusMap.a("5");
        }

        //更新动态树
        List<MindNode> list = jsonAnalyze.parseList(activeList);

        MindNode newMindNode = new MindNode();
        newMindNode.setNodeId(nodeId);
        newMindNode.setNodeName(nodeName);
        newMindNode.setParentId(parentId);

        list.add(mindNode);
        String open = mindMapService.openMind(list, rootId);

        mindMap.setMapList(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        //获取当前时间，将当前时间放入date
        mindMap.setData(nodeId);
        if(mindMapService.updateMindMap(mindMap)){
            return open;
        }
        return statusMap.a("2");
    }



    /**
     * 修改知识图谱节点信息
     * @param mindNode
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("updateMapNode")
    @ResponseBody
    public String updateMapNode(MindNode mindNode,
                                HttpServletRequest request) throws IOException{

        String nodeId = mindNode.getNodeId();
        String nodeName = mindNode.getNodeName();
        String rootId = mindNode.getRootId();

        HttpSession session = request.getSession();
        String userId =String.valueOf(session.getAttribute("userId"));

        if(userId == null || userId.equals("")){
            return statusMap.a("2");
        }


        MindMap mindMap = mindMapService.getMindMap(rootId);
        String activeList = mindMap.getMapList();
        String mindUser = mindMap.getUserId().toString();


        //防止其他人修改自己的知识图谱
        if (!userId.equals(mindUser)){
            return statusMap.a("5");
        }
        //更新动态树
        List<MindNode> list = jsonAnalyze.parseList(activeList);

        for(int i= 0; i<list.size(); i++){
            //存放根节点对应的思维导图数据
            MindNode getMindNode = list.get(i);

            if( getMindNode.getNodeId().equals(nodeId) ){
                getMindNode.setNodeName(nodeName);
                list.set(i, mindNode);          //修改
            }

        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //获取当前时间，将当前时间放入date
        mindMap.setUpdateTime(df.format(new Date()));
        //更改MindMap名
        if(rootId.equals(nodeId)){
            mindMap.setRootName(nodeName);
        }


        //打开知识图谱
        String open = mindMapService.openMind(list, rootId);
        mindMap.setMapList(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(mindMapService.updateMindMap(mindMap)){
            return statusMap.a("1");
        }
        return statusMap.a("3");

    }


    /**
     * 删除数据库（学习是不可能学习的，只是删库跑路才能勉强维持的了生活）
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("delMapNode")
    @ResponseBody
    public String delMapNode(@RequestBody String requestJsonBody,
                             HttpServletRequest request) throws IOException{

        Map<String, Object> map=jsonAnalyze.json2Map(requestJsonBody);
        String nodeId = String.valueOf(map.get("nodeId")); //获取节点id
        String rootId = String.valueOf(map.get("rootId"));//获取跟节点id

        HttpSession session=request.getSession();
        String userId= String.valueOf(session.getAttribute("userId")); //获取本用户id

        if (userId == null || userId.equals("")) {  //判断用户是否登录
            return statusMap.a("2");
        }

        //获得跟节点的整个思维导图
        MindMap mindMap1 = mindMapService.getMindMap(rootId);
        //获得思维导图主人
        String mindUser = mindMap1.getUserId().toString();

        //防止其他人修改自己的知识图谱
        if (!userId.equals(mindUser)){ //判断是否是自己的图谱
            return statusMap.a("5");
        }
        String activeList1 = mindMap1.getMapList();
        List<MindNode> list1 = jsonAnalyze.parseList(activeList1); //定义一个MindNode数组
        List<MindNode> list2 = new ArrayList<MindNode>();

        //用来存储取出要删除的子节点
        List<MindNode> list3 = mindMapService.getChild(list1, nodeId, list2);

        //循环处理数据
        for (Iterator it = list3.iterator(); it.hasNext();) { //迭代器使用方法iterator()要求容器返回一个Iterator。
            //第一次调用Iterator的next()方法时，它返回序列的第一个元素。
            //注意：iterator()方法是java.lang.Iterable接口,被Collection继承
            //使用next()获得序列中的下一个元素。
            //使用hasNext()检查序列中是否还有元素
            //使用remove()将迭代器新返回的元素删除
            MindNode mindNode = (MindNode) it.next();
            String id = mindNode.getNodeId();

            //List<UploadFile> fileList = null;
            try {
                //根据节点id把节点上的资源全部删除，即将is_del设置成1
                Integer fileNumber = fileService.deleteNodeFiles(id);
                logger.info(id + "节点上删除的资源数是：" + fileNumber);
            } catch (Exception e) {
                // TODO: handle exception
            }

            //将节点删除，即将is_del设置成1
            try {
                Integer nodeNumber=mindNodeService.deleteNode(id);
                if (nodeNumber > 0){
                    logger.info(id + "节点被删除");
                } else {
                    logger.info(id + "节点未删除，没有找到");
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        //若是删除整个图(将分享过的思维导图也一并删除掉,并删掉整个图)
        try {
            if( nodeId.equals(rootId) ) {
                Integer i = mindMapService.deleteMap(rootId);
                if(i > 0){
                    logger.info(mindMap1.getRootName() + "思维导图被删除");
                } else {
                    logger.info(mindMap1.getRootName() + "思维导图未删除，没有找到");
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        //这边更新数据
        List<MindNode> list4 = mindNodeService.getNope(list3, list1);
        //打开树
        String open = mindMapService.openMind(list4, rootId);

        mindMap1.setMapList(jsonAnalyze.list2Json(list4));    //更新树
        mindMap1.setData(open);                              //更新数据

        if(mindMapService.updateMindMap(mindMap1)){
            return statusMap.a("1");
        }
        return statusMap.a("3");
    }

    /**
     * 获取自己的知识图谱列表
     * @param request
     * @return
     */
    @RequestMapping("getMyMap")
    @ResponseBody
    public ResponseMsg getMyMap(@RequestBody HttpServletRequest request, String pageNum, String pageSize){

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));
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
        List<MindMap> list = mindMapService.getMapList(userId);
        PageInfo<MindMap> pageInfo = new PageInfo<>(list);
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setCode(200);
        responseMsg.setMsg("成功");
        responseMsg.setData(pageInfo);
        return responseMsg;
    }

    /**
     * 获取我的知识图谱总页数
     */
    /*@RequestMapping("getMyMapPage.do")
    @ResponseBody
    public Long getMyMapPage(@RequestBody String requestJsonBody,
                             HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String parentid=String.valueOf(map.get("parentid"));

        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        Long total=null;
        Integer pageSize=(Integer) map.get("pageSize");
        try {
            total=this.tryCatchNewMindService.countByOneMind("userid", userid);
            total=(total-1)/pageSize+1;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }

        if( total.equals("null")||total.equals(null) ){
            return null;
        }
        return total;

    }*/

    /**
     *打开某个具体的知识图谱
     * @param nodeid
     * @param request
     * @return
     */
    @RequestMapping("openMyMap")
    @ResponseBody
    public String openMyMap(@RequestParam String nodeid, HttpServletRequest request){

        //HttpSession session = request.getSession();
        //String userId = String.valueOf(session.getAttribute("userId"));
        MindMap mindMap = mindMapService.getMindMap(nodeid);
        return mindMap.getData();
    }

    /**
     * 编辑知识图谱----是否分享
     * @param rootId
     * @param isShare 0取消分享  1分享思维导图
     * @return
     * @throws IOException
     */
    @RequestMapping("shareMap")
    @ResponseBody
    public String shareMap(@RequestParam String rootId, String isShare, HttpServletRequest request) throws IOException{

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));

        MindMap mindMap = mindMapService.getMindMap(rootId);
        String mindUser = mindMap.getUserId().toString();
        //判断是否是用户本人权限
        if (!userId.equals(mindUser)){
            return statusMap.a("5");
        }

        if( Integer.parseInt(isShare) > 0){
            Integer i = mindMapService.shareMap(rootId);
            if(i > 0){
                return statusMap.a("1"); // 分享成功
            } else {
                return statusMap.a("2"); // 分享失败
            }
        } else {
            Integer i = mindMapService.deleteshareMap(rootId);
            if(i > 0){
                return statusMap.a("3"); // 取消分享成功
            } else {
                return statusMap.a("4"); // 取消分享失败
            }
        }
    }

    /**
     * 获取分享的知识图谱列表
     * @param pageNum
     * @param pageSize
     * @param request
     */
    @RequestMapping("getShareMap")
    @ResponseBody
    public ResponseMsg getShareMap(@RequestBody String pageNum, String pageSize, HttpServletRequest request){

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));
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
        List<MindMap> list = mindMapService.getShareMapList(userId);
        PageInfo<MindMap> pageInfo = new PageInfo<>(list);
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setCode(200);
        responseMsg.setMsg("成功");
        responseMsg.setData(pageInfo);
        return responseMsg;
    }



    /**
     * 获取分享的知识图谱总页数
     */
   /* @RequestMapping("getShareMapTotal.do")
    @ResponseBody
    public Long getShareMapTotal(@RequestBody String requestJsonBody,
                                 HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String sharetype=String.valueOf(map.get("sharetype"));

        Integer pageSize= (Integer) map.get("pageSize");
        Long total;
        try {
            total=this.tryCatchShareService.countShareByOne("sharetype", sharetype);
            total=(total-1)/pageSize+1;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }

        if( total.equals("null")||total.equals(null) ){
            return null;
        }

        return total;
    }*/


    /**
     * 打开分享过的知识图谱
     */
    /*@RequestMapping("openShareMap.do")
    @ResponseBody
    public String openShareMap(@RequestParam String userid,
                               @RequestParam String nodeid, HttpServletRequest request){

        HttpSession session = request.getSession();
        String uid = String.valueOf(session.getAttribute("username"));

        MindMap mindMap = tryCatchNewMindService.getMindMap("userid", userid, "nodeid", nodeid);

        return mindMap.getData();
    }*/


    /**
     * 在思维导图列表中，编辑按钮
     * @param isShare 0代表
     * @return
     */
   /* @RequestMapping("editMap")
    @ResponseBody
    public String delShareMap(@RequestBody String isShare ,HttpServletRequest request) throws IOException{

        HttpSession session=request.getSession();
        String userId=String.valueOf(session.getAttribute("userId"));

        if( userid.equals("null")|| userid.equals(null) ){
            return statusMap.a("2");           //尚未登录的
        }

        if( !userid.equals(deleteUser) ){
            return statusMap.a("3");           //没有删除权限的
        }

        Share share=null;
        try {
            share=this.tryCatchShareService.getshare("userid", userid, "zsdid", nodeid);

            if(share!=null){

                if(this.tryCatchShareService.delShare(share)){
                    return statusMap.a("1");      //删除成功
                }else{
                    return statusMap.a("5");      //删除失败
                }

            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return statusMap.a("10");

    }*/



    /**
     * @author Ragty
     * @param  获取选中节点后面的子节点
     * @serialData 2018.6.12
     * @param mindNodeTool
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("getMapChild.do")
    @ResponseBody
    public String getMapChild(MindNodeTool mindNodeTool,
                              HttpServletRequest request) throws IOException{

        String rootid = mindNodeTool.getRootid();
        String nodeid = mindNodeTool.getNodeid();

        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        MindMap mindMap = tryCatchNewMindService.getMindMap("nodeid", rootid);
        String activeList = mindMap.getMaplist();

        List<MindNode> list = jsonAnalyze.parseList(activeList);


        List<MindNode> list2 = new ArrayList<MindNode>();

        //用来存储取出选中节点及它的子节点
        List<MindNode> list3 = tryCatchNewMindService.getChild(list, nodeid, list2);

        //包装数据
        String open = tryCatchNewMindService.openChildMap(list3, nodeid, rootid);
        return open;
    }




    /**
     * @author Ragty
     * @param  根据一个节点获取整个知识图谱
     * @serialData 2018.6.12
     * @param mindNodeTool
     * @param request
     * @return
     */
    @RequestMapping("getCompleteMap.do")
    @ResponseBody
    public String getCompleteMap(MindNodeTool mindNodeTool,
                                 HttpServletRequest request){

        String rootid = mindNodeTool.getRootid();

        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        MindMap mindMap = tryCatchNewMindService.getMindMap("nodeid", rootid);

        return mindMap.getData();
    }



    /**
     * @author Ragty
     * @param  修改节点颜色
     * @serialData 2018.6.12
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("setMapColor.do")
    @ResponseBody
    public String setMapColor(@RequestBody String requestJsonBody,
                              HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String nodeid = String.valueOf(map.get("nodeid"));
        String color = String.valueOf(map.get("color"));
        String rootid = String.valueOf(map.get("rootid"));

        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        MindMap mindMap = tryCatchNewMindService.getMindMap("userid", userid, "nodeid", rootid);
        String  maplist = mindMap.getMaplist();
        String  mindUser = mindMap.getUserid();

        List<MindNode> list = jsonAnalyze.parseList(maplist);

        //权限
        if (!userid.equals(mindUser)){
            return statusMap.a("3");
        }

        //改颜色
        for(int i=0; i<list.size(); i++){
            MindNode mind = list.get(i);

            if( mind.getNodeid().equals(nodeid) ){
                mind.setColor(color);
                list.set(i, mind);
            }
        }

        //更新数据
        String open = tryCatchMindMapService.openMind(list, rootid);

        mindMap.setMaplist(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(tryCatchNewMindService.updateMindMap(mindMap)){
            return statusMap.a("1");
        }

        return statusMap.a("2");

    }



    /**
     * @author Ragty
     * @param  保存节点拖动后的位置
     * @serialData 2018.6.12
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("saveMapPosition.do")
    @ResponseBody
    public String saveMapPosition(@RequestBody String requestJsonBody,
                                  HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String beforeid=String.valueOf(map.get("beforeId"));
        String afterid=String.valueOf(map.get("afterId"));
        String rootid = String.valueOf(map.get("rootid"));

        HttpSession session=request.getSession();
        String userid=String.valueOf(session.getAttribute("username"));

        if( userid.equals("null")||userid.equals(null) ){
            return statusMap.a("2");
        }


        MindMap mindMap = tryCatchNewMindService.getMindMap("userid", userid, "nodeid", rootid);
        String  maplist = mindMap.getMaplist();
        String  mindUser = mindMap.getUserid();

        //权限
        if (!userid.equals(mindUser)){
            return statusMap.a("4");
        }


        List<MindNode> list = jsonAnalyze.parseList(maplist);

        //保存拖动后的位置
        for(int i=0; i<list.size(); i++){
            MindNode mind = list.get(i);

            if( mind.getNodeid().equals(beforeid) ){
                mind.setParentid(afterid);
                list.set(i, mind);
            }
        }

        //更新数据
        String open = tryCatchMindMapService.openMind(list, rootid);

        mindMap.setMaplist(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(tryCatchNewMindService.updateMindMap(mindMap)){
            return statusMap.a("1");
        }
        return statusMap.a("10");
    }




    /**
     * @author Ragty
     * @param  保存节点知识点
     * @serialData 2018.6.13
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("saveMapZsd.do")
    @ResponseBody
    public String saveMapZsd(@RequestBody String requestJsonBody,
                             HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String zsdid = String.valueOf(map.get("zsdid"));
        String zsdmc = String.valueOf(map.get("zsdmc"));

        String zsddate = String.valueOf(map.get("zsddate"));//张金鑫:在前台随便给zsddate设一个值（也可以不传）
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");  //获取当前时间，将当前时间放入zsddate
        zsddate=df.format(new Date()); //zsddate为当前时间
        String zsdms = String.valueOf(map.get("zsdms"));
        //查找传入的数据中是否含有 "粘贴完成请刷新"
        //获取截图路径的session
        HttpSession session=request.getSession();
        String imagepath = String.valueOf(session.getAttribute("writrMe"));
        if(zsdms.indexOf("粘贴完成请刷新")!=-1) {
            System.out.println("含有该字符串，执行替换操作");
            zsdms=zsdms.replace("粘贴完成请刷新", imagepath);
        }
        //zsdms=zsdms.replace("&lt;", "<");
        //zsdms=zsdms.replace("&gt;", ">");

        String rootid = String.valueOf(map.get("rootid"));


        String userid = String.valueOf(session.getAttribute("username"));

        Zsd zsd= new Zsd();
        zsd.setZsdid(zsdid);
        zsd.setZsddate(zsddate);
        zsd.setZsdmc(zsdmc);
        zsd.setZsdms(zsdms);
        zsd.setUserid(userid);

        MindMap mindMap = tryCatchNewMindService.getMindMap("nodeid", rootid);
        String mindUser = mindMap.getUserid();
        //禁止在别人的知识图谱里添加知识点
        if( !(userid.equals(mindUser)) ){
            return statusMap.a("3");
        }

        if(tryCatchZsdService.getZsd1("userid", "zsdid", userid, zsdid) == null){

            if(tryCatchZsdService.saveZsd(zsd)){
                return statusMap.a("1");
            } else {
                return statusMap.a("2");
            }

        } else {

            if(tryCatchZsdService.update(zsd)){

                return statusMap.a("1");
            } else {
                return statusMap.a("2");
            }

        }

    }




    /**
     * @author Ragty
     * @param  获取当前节点知识点
     * @serialData 2018.6.13
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("getMapZsd.do")
    @ResponseBody
    public String getZsd(@RequestBody String requestJsonBody,
                         HttpServletRequest request) throws IOException{
        Map<String, Object> map3 = jsonAnalyze.json2Map(requestJsonBody);
        String nodeid = String.valueOf(map3.get("nodeid"));
        String rootid = String.valueOf(map3.get("rootid"));

        MindMap mindMap = tryCatchNewMindService.getMindMap("nodeid", rootid);
        String mindUser = mindMap.getUserid();

        Map<String, String> map = new HashMap<String, String>();
        Map<String, Object> map2 = new HashMap<String, Object>();

        Zsd zsd=null;

        try {
            zsd = tryCatchZsdService.getZsd1("userid", "zsdid", mindUser, nodeid);
            map.put("zsdmc", zsd.getZsdmc());
            map.put("zsdms", zsd.getZsdms());
        } catch (Exception e) {
            // TODO: handle exception
        }

        map2.put("zsdid", nodeid);
        map2.put("userid", mindUser);
        map2.put("map", map);
        return jsonAnalyze.map2Json(map2);
    }




    /**
     * @author Ragty
     * @param  节点上传文件
     * @param nodeid
     * @param rootid
     * @param file
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("setMapUpload.do")
    @ResponseBody
    public String setMapUpload(@RequestParam("nodeid") String nodeid,
                               @RequestParam("rootid") String rootid,
                               @RequestParam("wenjian") MultipartFile file,
                               HttpServletRequest request, HttpServletResponse response) throws IOException{

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String uploadtima = df.format(new Date());
        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        MindMap mindMap = tryCatchNewMindService.getMindMap("nodeid", rootid);
        String mindUser = mindMap.getUserid();

        if (userid.equals("null") || userid.equals(null)) {
            return statusMap.a("2");
        }

        //禁止非本节点用户在节点上传文件
        if (! (userid.equals(mindUser)) ){
            //	System.out.println("到这里,,,,,");
            return statusMap.a("5");
        }

        SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
        String files = df2.format(new Date());
        String filename = file.getOriginalFilename();

        String zsdid = null;
        String zlid = null;
        try {
            UploadFile uploadFile = tryCatchUploadFileService.getUploadFile(
                    "userid", "filename", "firstStatus", userid, filename, "1");
            zsdid = uploadFile.getZsdid();
            zlid = uploadFile.getFiles();
        } catch (Exception e) {
            // TODO: handle exception
        }

        //判断资源页面有无上传过，没上传过的话（直接上传），否则直接添加数据
        if (tryCatchUploadFileService.haveUploadFile("userid", "filename",
                userid, filename)) {

            if(zsdid == null){
                UploadFile uploadFile = tryCatchUploadFileService.getUploadFile(
                        "userid", "filename", "firstStatus", userid, filename, "1");

                uploadFile.setZsdid(nodeid);
                uploadFile.setFirstStatus("0");

                UploadFile uploadFile1 = tryCatchUploadFileService
                        .getUploadFile("userid", "filename", "firstStatus",
                                userid, filename, "1");
                uploadFile1.setZsdid("1");

                if (tryCatchUploadFileService.saveUploadFile(uploadFile)) {
                    //System.out.println("资源界面已经上传过该文件");
                    tryCatchUploadFileService.updateUploadeFile(uploadFile1);
                    return statusMap.a("1");
                } else {
                    return statusMap.a("2");
                }


            } else if (zsdid != nodeid) {

                UploadFile uploadFile = tryCatchUploadFileService
                        .getUploadFile("userid", "filename", userid, filename);
                uploadFile.setZsdid(nodeid);
                uploadFile.setFirstStatus("0");

                if (tryCatchUploadFileService.haveUploadFile("files", "zsdid",
                        zlid, nodeid)) {
                    //System.out.println("防止在该节点重复上传文件");
                    return statusMap.a("3");
                }

                if (tryCatchUploadFileService.saveUploadFile(uploadFile)) {
                    //System.out.println("在新节点上传该文件");
                    return statusMap.a("1");
                } else {
                    return statusMap.a("2");
                }

            }

        }


        //直接上传文件
        String type = "";

        String fileExtension = file.getOriginalFilename().substring(
                file.getOriginalFilename().lastIndexOf(".") + 1,
                file.getOriginalFilename().length());
        if ((fileExtension.equals("bmp")) || (fileExtension.equals("drw"))
                || (fileExtension.equals("dxf"))
                || (fileExtension.equals("eps"))
                || (fileExtension.equals("gif"))
                || (fileExtension.equals("jpg"))
                || (fileExtension.equals("png"))
                || (fileExtension.equals("pcd"))
                || (fileExtension.equals("pcx"))) {
            type = "picture";
        } else if ((fileExtension.equals("avi"))
                || (fileExtension.equals("mpeg"))
                || (fileExtension.equals("mpg"))
                || (fileExtension.equals("dat"))
                || (fileExtension.equals("ra")) || (fileExtension.equals("rm"))
                || (fileExtension.equals("wmv"))
                || (fileExtension.equals("mp4"))
                || (fileExtension.equals("swf"))
                || (fileExtension.equals("f4v"))) {
            type = "video";
        } else if ((fileExtension.equals("cd"))
                || (fileExtension.equals("ogg"))
                || (fileExtension.equals("mp3"))
                || (fileExtension.equals("asf"))
                || (fileExtension.equals("wma"))
                || (fileExtension.equals("wav"))
                || (fileExtension.equals("rm"))
                || (fileExtension.equals("midi"))
                || (fileExtension.equals("ape"))) {
            type = "doc";
        } else {
            type = "other";
        }
        byte[] newsPageByte = file.getBytes();

        String fileURL = "C:\\upload";

        BeSaveFileUitl be = new BeSaveFileUitl();
        be.setFileExtension(fileExtension);
        be.setFilesByte(newsPageByte);
        be.setFileURL(fileURL);


        String Url2 = "";
        String Url3 = "";
        String[] string = this.fileUpload.saveFile(be);

        //代表文件上传成功
        if ("1".equals(string[0])) {
            String URL = string[1];
            // 文件的真实路径，将之代替截图路径存入
            String url = URL.replaceAll("\\\\", "/") + "." + fileExtension;

            Url3 = url; // 文件在服务器中的真实路径，用来删除
            Url2 = "/"+ url.split("C:/")[1]; //代表服务器中的地址

        }

        Blob blob = streamToBlob.toBlob(file.getInputStream());

        // 数据备份
        FileStream fileStream = new FileStream();
        fileStream.setFilename(filename);
        fileStream.setF_id(files);
        fileStream.setFileExtension(fileExtension);
        fileStream.setParentid("0");
        fileStream.setUserid(userid);
        fileStream.setTrueUrl(Url2);
        fileStream.setFileStream(blob);
        fileStream.setFileType(type);
        fileStream.setDelStatus("0");
        fileStream.setNodeid(nodeid);
        fileStream.setUploadTime(uploadtima);

        tryCatchFileStreamService.saveFileStream(fileStream);

        UploadFile uploadFile = new UploadFile();
        uploadFile.setFilename(filename);
        uploadFile.setFiles(files); // 文件id
        uploadFile.setFilepath(Url2); // 文件的输出路径（在服务器上的访问路径）
        uploadFile.setFileroot(Url3); // 文件在pc上的实际路径
        uploadFile.setZlms("该资料现在没有描述");
        uploadFile.setFiletype(type);
        uploadFile.setUploadtime(uploadtima);
        uploadFile.setZsdid(nodeid);
        uploadFile.setUserid(userid);
        uploadFile.setF_parentid("0");
        uploadFile.setFirstStatus("1");
        if (this.tryCatchUploadFileService.saveUploadFile(uploadFile)) {
            //System.out.println("第一次在节点上传该文件");
            return statusMap.a("1");
        }
        return statusMap.a("2");

    }



    /**
     * @author Ragty
     * @param  获取本节点用户的上传文件（这里不加权限，谁都可以看到）
     * @serialData 2018.6.13
     * @param mindNodeTool
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("getMapUpload.do")
    @ResponseBody
    public String getMapUpload(MindNodeTool mindNodeTool,
                               HttpServletRequest request) throws IOException{

        String nodeid = mindNodeTool.getNodeid();
        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        String uploadzl = "success";
        String state = null;

        List<UploadFile> list = tryCatchUploadFileService.getUploadeFile(
                "userid", userid, "zsdid", nodeid);
        List<Map<String, String>> list2 = new ArrayList<Map<String,String>>();

        if ((list == null) || (list.equals(null))) {
            state = "1";
        } else {
            state = "0";
            for(int i = 0; i < list.size(); i++){
                UploadFile uploadFile = list.get(i);
                Map<String, String> map = new HashMap<String, String>();

                String type = uploadFile.getFiletype();
                String tubiao = "";
                if (type.equals("doc")) {
                    tubiao = "/assets/avatars/word.jpg";
                } else if (type.equals("video")) {
                    tubiao = "/assets/avatars/shipin.jpg";
                } else if (type.equals("picture")) {
                    tubiao = "/assets/avatars/tupian.jpg";
                } else if (type.equals("other")) {
                    tubiao = "/assets/avatars/tuzhi.jpg";
                } else if (type.equals("5")) {
                    tubiao = "/assets/avatars/yinpin.jpg";
                }

                map.put("zlmc", uploadFile.getFilename());
                map.put("tubiao", tubiao);
                map.put("zlms", uploadFile.getZlms()); // 资料描述
                map.put("zlid", String.valueOf(uploadFile.getFiles()));
                map.put("filepath", uploadFile.getFilepath());
                map.put("time", uploadFile.getUploadtime());
                map.put("fileType", uploadFile.getFiletype());

                if (list2.size() < list.size()){
                    list2.add(map);
                }

            }   //<!-- for -->

        }    //<!-- else -->

        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("uploadzl", uploadzl);
        map2.put("state", state);
        map2.put("list2", list2);
        map2.put("nodeid", nodeid);

        return jsonAnalyze.map2Json(map2);

    }



    /**
     * @author Ragty
     * @param  删除节点上的上传文件
     * @serialData 2018.6.13
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("delMapUpload.do")
    @ResponseBody
    public String delMapUpload(@RequestBody String requestJsonBody,
                               HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String zlid = String.valueOf(map.get("zlid"));
        String nodeid = String.valueOf(map.get("nodeid"));
        String rootid = String.valueOf(map.get("rootid"));

        HttpSession session = request.getSession();
        String userid = String.valueOf(session.getAttribute("username"));

        if (userid.equals("") || userid == null){
            return statusMap.a("2");
        }

        MindMap mindMap = tryCatchNewMindService.getMindMap("nodeid", rootid);
        String mindUser = mindMap.getUserid();

        //禁止非本节点用户在节点上传文件
        if (! (userid.equals(mindUser)) ){
            //System.out.println("到这里,,,,,");
            return statusMap.a("5");
        }

        UploadFile uploadFile = null;
        FileShare fileShare = null;
        FileCollection fileCollection = null;
        String realPath = null;

        try {
            uploadFile = this.tryCatchUploadFileService.getUploadFile("zsdid",
                    "files", nodeid, zlid);
            fileShare = this.tryCatchFileShareService.getFileShare("nodeid",
                    nodeid, "f_id", zlid);
            fileCollection = this.tryCatchFileCollectionService
                    .getFileCollection1("nodeid", nodeid, "f_id", zlid);
            realPath = uploadFile.getFileroot();
        } catch (Exception e) {
            // TODO: handle exception
        }

        // 状态是1，真实删除所有文件，状态是0或者null就删除节点上的
        if(uploadFile.getFirstStatus().equals("1")){

            File file = new File(realPath);

            if(!file.exists()){
                //System.out.println("文件不存在");
            } else{
                //System.out.println("即将删除文件");
                file.delete();
                //System.out.println("已删除文件");
            }

            //修改回收站状态
            try {
                FileStream fileStream = tryCatchFileStreamService.getFileStream1(
                        "userid", "f_id", userid, zlid);

                if (!fileStream.equals("null")) {
                    fileStream.setDelStatus("1");
                    tryCatchFileStreamService.updateFileStream(fileStream);
                }


            } catch (Exception e) {
                // TODO: handle exception
            }


            //清空相关数据
            if ((tryCatchUploadFileService.delAllUploadFile("userid", "files",
                    userid, zlid))
                    | (tryCatchFileShareService.delAllFileShare("userid",
                    userid, "f_id", zlid))
                    | (tryCatchFileCollectionService.delAllFileCollection(
                    "userid", userid, "f_id", zlid))) {
                //System.out.println("大清洗式的删除");
                return statusMap.a("1");
            }
            return statusMap.a("3");

        } else {

            //只删除与该节点上传文件有关的数据
            if (uploadFile != null){
                tryCatchUploadFileService.deleteUploadFile(uploadFile);
            }
            if(fileShare != null){
                tryCatchFileShareService.delShareFile(fileShare);
            }
            if(fileCollection != null){
                tryCatchFileCollectionService.delFileCollection(fileCollection);
            }
            return statusMap.a("1");
        }

    }


    /**
     * @author Ragty
     * @param  乾坤大挪移
     * @serialData 2018.6.13
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("changeData.do")
    @ResponseBody
    public String changeData(@RequestBody String requestJsonBody,
                             HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String xx = String.valueOf(map.get("xx"));

        List<MindNode> mapList = tryCatchMindMapService.getMindNode("parentid", "00100");

        //遍历所有图
        for(Iterator it = mapList.iterator(); it.hasNext();){

            MindNode mind = (MindNode) it.next();
            String nodeid = mind.getNodeid();
            String color = mind.getColor();
            String type = mind.getType();
            String nodename = mind.getNodename();
            String userid = mind.getUserid();
            String realname = null;
            try {
                realname = tryCatchUserService.getUserByNickname(userid).getRealName();
            } catch (Exception e) {
                // TODO: handle exception
            }


            List<MindNode>  listDemo = tryCatchMindMapService.getMindNode("type", type);
            String open = tryCatchMindMapService.openMind(listDemo, type);

            MindMap mindMap = new MindMap();
            mindMap.setDate(nodeid);
            mindMap.setData(open);
            mindMap.setMaplist(jsonAnalyze.list2Json(listDemo));
            mindMap.setNodeid(nodeid);
            mindMap.setRealname(realname);
            mindMap.setUserid(userid);
            mindMap.setNodename(nodename);

            tryCatchNewMindService.saveMindMap(mindMap);

        }

        return statusMap.a("1");
    }



    /**
     * @author Ragty
     * @param  教师获取所有用户的知识图谱
     * @serialData 2018.6.14
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("getAllMap.do")
    @ResponseBody
    public String getAllMap(@RequestBody String requestJsonBody,
                            HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        Integer currentPage= (Integer) map.get("currentPage");
        Integer pageSize= (Integer) map.get("pageSize");


        List<MindMap> list = tryCatchNewMindService.getAllMap(currentPage, pageSize);

        if(list.equals(null) || list.equals("null")){
            return statusMap.a("3");
        }

        return jsonAnalyze.list2Json(list);
    }



    /**
     * @author Ragty
     * @param  获取教师端知识图谱总页数
     * @serialData 2018.6.14
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("getAllMapPage.do")
    @ResponseBody
    public Long getAllMapPage(@RequestBody String requestJsonBody,
                              HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);

        Integer pageSize= (Integer) map.get("pageSize");
        Long total=null;

        try {
            total = tryCatchNewMindService.getAllMapPage();
            //System.out.println(total);
            total = (total-1)/pageSize+1;
        } catch (Exception e) {
            // TODO: handle exception
        }

        if( total.equals("null")||total.equals(null) ){
            return null;
        }

        return total;

    }



    /**
     * @author Ragty
     * @param  获取教师端查询后的数据
     * @serialData 2018.6.14
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("findTeacherMap.do")
    @ResponseBody
    public String findTeacherMap(@RequestBody String requestJsonBody,
                                 HttpServletRequest request) throws IOException{

        Map<String, Object> map =jsonAnalyze.json2Map(requestJsonBody);
        String queryMessage =  String.valueOf(map.get("queryMessage"));
        Integer currentPage  = (Integer) map.get("currentPage");
        Integer pageSize = (Integer) map.get("pageSize");

        HttpSession session=request.getSession();
        String username=String.valueOf(session.getAttribute("username"));

        if( (username.equals("null"))||(username.equals(null)) ){
            return statusMap.a("2");
        }

        List<MindMap> list = tryCatchNewMindService.queryTeacherMap(queryMessage, currentPage, pageSize);

        if( list == null ){
            return statusMap.a("3");
        }

        return jsonAnalyze.list2Json(list);
    }



    /**
     * @author Ragty
     * @param  获取查询后的知识图谱的页数
     * @param requestJsonBody
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("findTeacherMapPage.do")
    @ResponseBody
    public Long findTeacherMapPage(@RequestBody String requestJsonBody,
                                   HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String queryMessage = String.valueOf(map.get("queryMessage"));

        Integer pageSize = (Integer) map.get("pageSize");

        Long total=null;

        total= tryCatchNewMindService.queryTeacherMapPage(queryMessage);
        total=(total-1)/pageSize+1;

        if( total.equals("null")||total.equals(null) ){
            return null;
        }
        return total;

    }
}
