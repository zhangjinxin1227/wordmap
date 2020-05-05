package com.zjx.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/mindMap")
public class MindMapController {

    @Resource
    private StreamToBlob streamToBlob;
    @Resource
    private JsonAnalyze jsonAnalyze;
    @Resource
    private StatusMap statusMap;
    @Resource
    private MindMapService mindMapService;
    @Resource
    private FileService fileService;
    @Resource
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
    @RequestMapping(value = "newNewMind" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        mindmap.setIntegral(0);
        mindmap.setLogicGrade(0);
        mindmap.setTotalGrade(0);
        mindmap.setMemoryGrade(0);
        mindmap.setArtisticGrade(0);


        if(mindMapService.saveMindMap(mindmap)){
            //用户每次新建导图时都会增加一个积分
            mindMapService.newMapToken(userId);
            return data;
        }else {
            return statusMap.a("2");
        }
    }



    /**
     * @author 张金鑫
     * 新建子节
     * @param mindNode
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/saveMapNode" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        newMindNode.setUserId(Integer.parseInt(userId));

        list.add(newMindNode);
        //可能有问题
        String open = mindMapService.openMind(list, rootId);

        mindMap.setMapList(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(mindMapService.updateMindMap(mindMap)){
            //将新建的节点存入数据库
            mindNodeService.insertNode(newMindNode);
            return open;
        }
        return statusMap.a("2");
    }



    /**
     * 修改知识图谱节点名称
     * @param mindNode
     * @param request
     * @return
     * @throws IOException
     */
        @RequestMapping(value = "updateMapNode" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
                //list.set(i, mindNode);          //修改
            }

        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //获取当前时间，将当前时间放入date
        mindMap.setUpdateTime(df.format(new Date()));
        //更改MindMap名
        if(rootId.equals(nodeId)){
            mindMapService.updateMapName(nodeId, nodeName);
            mindMap.setRootName(nodeName);
        }


        //打开知识图谱
        String open = mindMapService.openMind(list, rootId);
        mindMap.setMapList(jsonAnalyze.list2Json(list));    //更新树
        mindMap.setData(open);                              //更新数据

        if(mindMapService.updateMindMap(mindMap)){
            mindNodeService.updateNodeName(nodeId,nodeName);
            return statusMap.a("1");
        }
        return statusMap.a("3");

    }



    /**
     * 删除节点信息（学习是不可能学习的，只是删库跑路才能勉强维持的了生活）
     * @param delMindNode
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "delMapNode" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String delMapNode(@RequestBody MindNode delMindNode, HttpServletRequest request) throws IOException{

        String nodeId = delMindNode.getNodeId(); //获取节点id
        String rootId = delMindNode.getRootId();//获取跟节点id

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
                e.printStackTrace();
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
                e.printStackTrace();
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
     * @param
     * @return
     */
    @RequestMapping(value = "getMyMap" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseMsg getMyMap(@RequestBody Map<String, String> map,HttpServletRequest request){

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));
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
        List<MindMap> list = mindMapService.getMapList(userId);
        PageInfo<MindMap> pageInfo = new PageInfo<>(list);
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setCode(200);
        responseMsg.setMsg("成功");
        responseMsg.setData(pageInfo);
        return responseMsg;
    }



    /**
     *打开某个具体的知识图谱
     * @param nodeId
     * @param request
     * @return
     */
    @RequestMapping(value = "openMyMap" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String openMyMap(@RequestParam String nodeId, HttpServletRequest request){

        MindMap mindMap = mindMapService.getMindMap(nodeId);
        return mindMap.getData();
    }



    /**
     * 编辑知识图谱----是否分享
     * @param map
     *   0取消分享  1分享思维导图
     */
    @RequestMapping(value = "shareMap" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String shareMap(@RequestBody Map<String, String> map, HttpServletRequest request) throws IOException{

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));

        String rootId = map.get("rootId");
        String isShare = map.get("isShare");
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
    @RequestMapping(value = "getShareMap" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
     * 获取选中节点后面的子节点
     * @param mindNode
     * @return
     */
    @RequestMapping(value = "getMapChild" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getMapChild(@RequestBody MindNode mindNode, HttpServletRequest request){

        String rootId = mindNode.getRootId();
        String nodeId = mindNode.getNodeId();

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
    @RequestMapping(value = "getCompleteMap" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getCompleteMap(String rootId, HttpServletRequest request){

        MindMap mindMap = mindMapService.getMindMap(rootId);
        return mindMap.getData();
    }


    /**
     * 修改节点颜色
     * @param mindNode
     * @param request
     * @return
     */
    @RequestMapping(value = "setMapColor" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String setMapColor(@RequestBody MindNode mindNode, HttpServletRequest request) throws IOException{

        String nodeId = mindNode.getNodeId();
        String color = mindNode.getColor();
        String rootId = mindNode.getRootId();

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
            mindNodeService.setMapColor(nodeId,color);
            return statusMap.a("1");
        }
        return statusMap.a("2");

    }



    /**
     *  保存节点拖动后的位置
     * @param requestJsonBody
     * @param request
     */
    @RequestMapping(value = "saveMapPosition" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String saveMapPosition(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String beforeId=String.valueOf(map.get("beforeId"));
        String afterId=String.valueOf(map.get("afterId"));
        String rootId = String.valueOf(map.get("rootId"));
        String nodeId = String.valueOf(map.get("nodeId"));

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

        mindNodeService.updateNodeParentId(nodeId, afterId);

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
    @RequestMapping(value = "saveMapZsd" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
            return statusMap.a("1");
        }else {
            return statusMap.a("2");
        }
    }


    /**
     * 获取当前节点知识点
     * @param requestJsonBody
     * @param request
     */
    @RequestMapping(value = "getMapZsd" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @RequestMapping(value = "setMapUpload" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String setMapUpload(@RequestParam("nodeId") String nodeId,
                               @RequestParam("rootId") String rootId,
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
            String tubiao = "";
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

                if ((preFix2.equals("bmp")) || (preFix2.equals("drw"))
                        || (preFix2.equals("dxf"))
                        || (preFix2.equals("eps"))
                        || (preFix2.equals("gif"))
                        || (preFix2.equals("jpg"))
                        || (preFix2.equals("png"))
                        || (preFix2.equals("pcd"))
                        || (preFix2.equals("pcx"))) {
                    preFix2 = "picture";
                    tubiao = "/assets/avatars/tupian.jpg";
                } else if ((preFix2.equals("avi"))
                        || (preFix2.equals("mpeg"))
                        || (preFix2.equals("mpg"))
                        || (preFix2.equals("dat"))
                        || (preFix2.equals("ra")) || (preFix2.equals("rm"))
                        || (preFix2.equals("wmv"))
                        || (preFix2.equals("mp4"))
                        || (preFix2.equals("swf"))
                        || (preFix2.equals("f4v"))) {
                    preFix2 = "video";
                    tubiao = "/assets/avatars/shipin.jpg";
                } else if ((preFix2.equals("cd"))
                        || (preFix2.equals("ogg"))
                        || (preFix2.equals("mp3"))
                        || (preFix2.equals("asf"))
                        || (preFix2.equals("wma"))
                        || (preFix2.equals("wav"))
                        || (preFix2.equals("rm"))
                        || (preFix2.equals("midi"))
                        || (preFix2.equals("ape"))) {
                    preFix2 = "doc";
                    tubiao = "/assets/avatars/word.jpg";
                } else {
                    preFix2 = "other";
                    tubiao = "/assets/avatars/tuzhi.jpg";
                }

                //path = ""+path;
                //将文件路径存入数据库
                UploadFile uploadFile = new UploadFile();
                uploadFile.setUserId(Integer.parseInt(userId));
                uploadFile.setFileName(preFix1);
                uploadFile.setFilePath(path);
                uploadFile.setFileType(preFix2);
                uploadFile.setTubiao(tubiao);
                uploadFile.setNodeId(nodeId);
                fileService.insertNodeFile(uploadFile);
            } catch (Exception e) {
                logger.info("---------文件上传失败————————");
                e.printStackTrace();
                return statusMap.a("2");
            }
        }

        return statusMap.a("1");
    }



    /**
     * 获取本节点用户的上传文件（这里不加权限，谁都可以看到）
     * @serialData 2018.6.13
     * @param uploadFile
     * @param request
     */
    @RequestMapping(value = "getMapUpload" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseMsg getMapUpload(@RequestBody UploadFile uploadFile, HttpServletRequest request) throws IOException{

        String nodeId = uploadFile.getNodeId();
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
    @RequestMapping(value = "delMapUpload" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String delMapUpload(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{

        Map<String, Object> map = jsonAnalyze.json2Map(requestJsonBody);
        String fileId = String.valueOf(map.get("fileId"));

        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));

        Integer i = fileService.deleteFileId(Integer.parseInt(fileId), Integer.parseInt(userId));
        if(i > 0){
            return statusMap.a("1");
        }else {
            return statusMap.a("2");
        }
    }



}
