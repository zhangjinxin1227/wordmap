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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public String saveMapNode(MindNode mindNode, HttpServletRequest request) throws IOException{

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
    public String updateMapNode(MindNode mindNode, HttpServletRequest request) throws IOException{

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
    public String delMapNode(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

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
     * 获取选中节点后面的子节点
     * @param mindNode
     * @return
     */
    @RequestMapping("getMapChild")
    @ResponseBody
    public String getMapChild(@RequestBody MindNode mindNode, HttpServletRequest request){

        String rootId = mindNode.getRootId();
        String nodeId = mindNode.getNodeId();

        //HttpSession session = request.getSession();
        //String userId = String.valueOf(session.getAttribute("userId"));

        MindMap mindMap = mindMapService.getMindMap(rootId);
        String activeList = mindMap.getMapList();

        List<MindNode> list = jsonAnalyze.parseList(activeList);

        List<MindNode> list2 = new ArrayList<MindNode>();

        //用来存储取出选中节点及它的子节点
        List<MindNode> list3 = mindMapService.getChild(list, nodeId, list2);

        //包装数据
        String open = null;
        try {
            open = mindMapService.openChildMap(list3, nodeId, rootId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return open;
    }



    /**
     * 根据一个节点获取整个知识图谱
     * @param rootId
     * @param request
     * @return
     */
    @RequestMapping("getCompleteMap")
    @ResponseBody
    public String getCompleteMap(String rootId, HttpServletRequest request){

        MindMap mindMap = mindMapService.getMindMap(rootId);
        return mindMap.getData();
    }


    /**
     * 修改节点颜色
     * @param requestJsonBody
     * @param request
     * @return
     */
    @RequestMapping("setMapColor")
    @ResponseBody
    public String setMapColor(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String nodeId = String.valueOf(map.get("nodeId"));
        Integer color = Integer.parseInt(map.get("color").toString());
        String rootId = String.valueOf(map.get("rootId"));

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));

        MindMap mindMap = mindMapService.getMindMap(rootId);
        String  maplist = mindMap.getMapList();
        String  mindUser = mindMap.getUserId().toString();

        List<MindNode> list = jsonAnalyze.parseList(maplist);

        //权限
        if (!userId.equals(mindUser)){
            return statusMap.a("3");
        }

        //改颜色
        for(int i=0; i<list.size(); i++){
            MindNode mind = list.get(i);
            if( mind.getNodeId().equals(nodeId) ){
                mind.setColor(color);
                list.set(i, mind);
            }
        }

        //更新数据
        String open = mindMapService.openMind(list, rootId);

        mindMap.setMapList(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(mindMapService.updateMindMap(mindMap)){
            return statusMap.a("1");
        }
        return statusMap.a("2");

    }



    /**
     *  保存节点拖动后的位置
     * @param requestJsonBody
     * @param request
     */
    @RequestMapping("saveMapPosition")
    @ResponseBody
    public String saveMapPosition(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String beforeId=String.valueOf(map.get("beforeId"));
        String afterId=String.valueOf(map.get("afterId"));
        String rootId = String.valueOf(map.get("rootId"));

        HttpSession session=request.getSession();
        String userId=String.valueOf(session.getAttribute("userId"));

        if( userId == null || userId.equals("")){
            return statusMap.a("2");
        }

        MindMap mindMap = mindMapService.getMindMap(rootId);
        String  mapList = mindMap.getMapList();
        String  mindUser = mindMap.getUserId().toString();

        //权限
        if (!userId.equals(mindUser)){
            return statusMap.a("4");
        }

        List<MindNode> list = jsonAnalyze.parseList(mapList);

        //保存拖动后的位置
        for(int i=0; i<list.size(); i++){
            MindNode mind = list.get(i);
            if( mind.getNodeId().equals(beforeId) ){
                mind.setParentId(afterId);
                list.set(i, mind);
            }
        }

        //更新数据
        String open = mindMapService.openMind(list, rootId);

        mindMap.setMapList(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(mindMapService.updateMindMap(mindMap)){
            return statusMap.a("1");
        }
        return statusMap.a("10");
    }



    /**
     * 保存节点知识点
     * @param requestJsonBody
     * @param request
     */
    @RequestMapping("saveMapZsd")
    @ResponseBody
    public String saveMapZsd(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String ndoeId = String.valueOf(map.get("nodeId"));
        String zsdms = String.valueOf(map.get("zsdms"));
        String rootId = String.valueOf(map.get("rootId"));

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));
        MindMap mindMap = mindMapService.getMindMap(rootId);
        String mindUser = mindMap.getUserId().toString();

        //禁止在别人的知识图谱里添加知识点
        if( !(userId.equals(mindUser)) ){
            return statusMap.a("3");
        }

        MindNode mindNode = new MindNode();
        mindNode.setZsdms(zsdms);
        mindNode.setNodeId(ndoeId);

        //将知识点描述保存
        Integer i = mindNodeService.saveMapZsd(mindNode);
        if(i > 0){
            return "保存成功";
        }else {
            return "保存失败";
        }
    }


    /**
     * 获取当前节点知识点
     * @param requestJsonBody
     * @param request
     */
    @RequestMapping("getMapZsd")
    @ResponseBody
    public String getZsd(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{
        Map<String, Object> map3 = jsonAnalyze.json2Map(requestJsonBody);
        String nodeId = String.valueOf(map3.get("nodeId"));
        String rootId = String.valueOf(map3.get("rootId"));

        Map<String, String> map = new HashMap<String, String>();
        Map<String, Object> map2 = new HashMap<String, Object>();

        try {
            MindNode mindNode = mindNodeService.getNodems(nodeId);
            map.put("nodeName", mindNode.getNodeName());
            map.put("zsdms", mindNode.getZsdms());
        } catch (Exception e) {
            // TODO: handle exception
        }

        map2.put("nodeId", nodeId);
        map2.put("map", map);
        return jsonAnalyze.map2Json(map2);
    }


    /**
     * 节点上传文件
     * @param nodeId
     * @param rootId
     * @param files
     */
    @RequestMapping("setMapUpload")
    @ResponseBody
    public String setMapUpload(@RequestParam("nodeid") String nodeId,
                               @RequestParam("rootid") String rootId,
                               @RequestParam("file") MultipartFile[] files,
                               HttpServletRequest request, HttpServletResponse response) throws IOException{

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String uploadtima = df.format(new Date());
        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));

        MindMap mindMap = mindMapService.getMindMap(rootId);
        String mindUser = mindMap.getUserId().toString();

        if (userId == null || userId.equals("")) {
            return statusMap.a("2");
        }

        //禁止非本节点用户在节点上传文件
        if (! (userId.equals(mindUser)) ){
            return statusMap.a("5");
        }

        Calendar cal = Calendar.getInstance();
        String month = (cal.get(Calendar.MONTH)+1)+"";
        String year = (cal.get(Calendar.YEAR))+"";

        String pathUrl = "D:\\mindMapFile";
        File newFile = new File(pathUrl+File.separator+year+File.separator+month);

        newFile.mkdirs();//判断年 月 日文件夹是否创建，没有则新建

        for (int i = 0;i < files.length; i++){ //遍历文件

            String fileName = files[i].getOriginalFilename(); //获取文件全名称
            String preFix1 = fileName.substring(0,fileName.indexOf("."));//获得文件名称
            String preFix2 = fileName.substring(fileName.lastIndexOf(".")+1);//获得文件类型
            InputStream in = null;

            try {
                in = files[i].getInputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                String path=pathUrl+File.separator+year+File.separator+month+File.separator+fileName;

                File file = new File(path);
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();//创建父级文件路径
                    file.createNewFile();//创建文件
                }
                FileOutputStream out = new FileOutputStream(path);

                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                in.close();

                //将文件路径存入数据库
                UploadFile uploadFile = new UploadFile();
                uploadFile.setUserId(Integer.parseInt(userId));
                uploadFile.setFileName(preFix1);
                uploadFile.setFilePath(path);
                uploadFile.setFileType(preFix2);
                uploadFile.setNodeId(nodeId);
                fileService.insertNodeFile(uploadFile);
            } catch (Exception e) {
                logger.info("---------文件上传失败————————");
                e.printStackTrace();
                return "上传文件失败";
            }
        }

        return "上传文件成功";
    }



    /**
     * 获取本节点用户的上传文件（这里不加权限，谁都可以看到）
     * @serialData 2018.6.13
     * @param mindNode
     * @param request
     */
    @RequestMapping("getMapUpload")
    @ResponseBody
    public ResponseMsg getMapUpload(MindNode mindNode,
                               HttpServletRequest request) throws IOException{

        String nodeId = mindNode.getNodeId();
        //HttpSession session = request.getSession();
        //String userId = String.valueOf(session.getAttribute("userId"));
        ResponseMsg responseMsg = new ResponseMsg();

        List<UploadFile> list = fileService.getUploadeFile(nodeId);

        if ((list == null) || (list.equals(null))) {
            responseMsg.setCode(500);
            responseMsg.setMsg("尚未上传文件");
            return responseMsg;
        } else {
            responseMsg.setCode(200);
            responseMsg.setMsg("获得成功");
            responseMsg.setData(list);
            return responseMsg;
        }

    }



    /**
     * 删除节点上的上传文件
     * @param requestJsonBody
     */
    @RequestMapping("delMapUpload")
    @ResponseBody
    public String delMapUpload(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String fileId = String.valueOf(map.get("fileId"));
        String rootId = String.valueOf(map.get("rootId"));

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));

        if (userId == null || userId.equals("")){
            return statusMap.a("2");
        }

        MindMap mindMap = mindMapService.getMindMap(rootId);
        String mindUser = mindMap.getUserId().toString();

        //禁止非本节点用户在节点上传文件
        if (! (userId.equals(mindUser)) ){
            return statusMap.a("5");
        }
        Integer i = fileService.deleteFileId(Integer.parseInt(fileId));
        if(i > 0){
            return "删除成功";
        }else {
            return "删除失败";
        }
    }


    /**
     *   乾坤大挪移
     * @param requestJsonBody
     * @param request
     */
    /*@RequestMapping("changeData.do")
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
    }*/

}
