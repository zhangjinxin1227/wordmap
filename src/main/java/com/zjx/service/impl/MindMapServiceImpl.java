package com.zjx.service.impl;

import com.zjx.json.JsonAnalyze;
import com.zjx.mapper.MindMapMapper;
import com.zjx.model.MindMap;
import com.zjx.model.MindNode;
import com.zjx.model.MindNode2Util;
import com.zjx.model.Node2;
import com.zjx.service.MindMapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service
public class MindMapServiceImpl implements MindMapService {

    @Resource
    private MindMapMapper mindMapMapper;
    @Resource
    private JsonAnalyze jsonAnalyze;

    //新建思维导图
    @Override
    public Boolean saveMindMap(MindMap mindMap){
        boolean flag = mindMapMapper.saveMindMap(mindMap);
        return true;
    }

    //根据根节点id获得整个思维导图
    @Override
    public MindMap getMindMap(String rootId){
        return mindMapMapper.getMindMap(rootId);
    }


    /**
     *  打开知识图谱
     * @param list  (查询到的知识图谱的list)
     * @param type  (知识图谱的类型，用于将知识图谱类型存往前台)
     * @throws IOException
     */
    @Override
    public String  openMind(List<MindNode> list, String type) throws IOException{

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("name", "jsMind remote");
        meta.put("author", "hizzgdev@163.com");
        meta.put("version", "0.2");

        data.put("meta", meta);
        data.put("format", "node_tree");

        List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();

        for(int i=0; i<list.size(); i++){
            Map<String, Object> map = new HashMap<String, Object>();
            MindNode mindNode =list.get(i);
            map.put("id", mindNode.getNodeId());
            map.put("topic", mindNode.getNodeName());
            map.put("parentid", mindNode.getParentId());
            map.put("color", mindNode.getColor());
            list2.add(map);
        }

        List dataList = list2;
        HashMap nodeList = new HashMap();
        Node2 root = null;
        MindNode2Util mindNode2Util = new MindNode2Util();

        for (Iterator it = dataList.iterator(); it.hasNext();) {
            Map dataRecord = (Map) it.next();
            Node2 node = new Node2();
            node.id = ((String) dataRecord.get("id"));
            node.topic = ((String) dataRecord.get("topic"));
            node.parentid = ((String) dataRecord.get("parentid"));
            node.color = (String) dataRecord.get("color");
            nodeList.put(node.id, node);
        }


        Set entrySet = nodeList.entrySet();
        for(Iterator it = entrySet.iterator(); it.hasNext();){
            Node2 node = (Node2) ((Map.Entry) it.next()).getValue();

            if ((node.parentid == null) || (node.parentid.equals("00100"))) {
                root = node;
            } else {
                try {
                    ((Node2) nodeList.get(node.parentid)).addChild(node); // 重点，在主节点后面加子节点
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

        }

        mindNode2Util.setState("1");

        data.put("data", root.toString());

        String datas = this.jsonAnalyze.object2Json(data).toString();

        datas = datas.replace("\"", "'");
        datas = datas.replace(" ", "");
        datas = datas.replace("'{", "{");
        datas = datas.replace("}'", "}");
        mindNode2Util.setDatas(datas);
        mindNode2Util.setKcmc(type);
        mindNode2Util.setMindJson2("success");

        return this.jsonAnalyze.object2Json(mindNode2Util);

    }


    //更新思维导图
    @Override
    public Boolean updateMindMap(MindMap mindMap){
        Integer i = mindMapMapper.updateMindMap(mindMap);
        if (i > 0){
            return true;
        }else {
            return false;
        }
    }

    /**
     * @获取所有子节点
     * @param list
     * @param nodeid
     * @param storage
     * @return
     */
    @Override
    public List<MindNode> getChild(List<MindNode> list, String nodeid, List<MindNode> storage){
        //这句话貌似没用
        //judgeHaveChild(list, nodeid, storage);

        for(Iterator it = list.iterator(); it.hasNext();){
            MindNode mindNode = (MindNode) it.next();
            if(mindNode.getNodeId().equals(nodeid)){
                storage.add(mindNode);
            }
        }
        return storage;
    }

    /**
     * 删除整个思维导图
     * @param rootId
     * @return
     */
    @Override
    public Integer deleteMap(String rootId){
        return mindMapMapper.deleteMap(rootId);
    }

    /**
     * 获得用户所有思维导图列表
     * @param userId
     * @return
     */
    @Override
    public List<MindMap> getMapList(String userId){
        return mindMapMapper.getMapList(Integer.parseInt(userId));
    }

    /**
     * 分享思维导图
     * @param rootId
     * @return
     */
    @Override
    public Integer shareMap(String rootId){
        return mindMapMapper.shareMap(rootId);
    }

    /**
     * 取消分享思维导图
     * @param rootId
     * @return
     */
    @Override
    public Integer deleteshareMap(String rootId){
        return mindMapMapper.deleteshareMap(rootId);
    }

    /**
     * 获得分享的思维导图列表
     * @param userId
     * @return
     */
    @Override
    public List<MindMap> getShareMapList(String userId){
        return mindMapMapper.getShareMapList(Integer.parseInt(userId));
    }

    /**
     * 打开子节点的知识图
     * @param list
     * @param nodeid
     * @param rootid
     * @return
     */
    @Override
    public String openChildMap(List<MindNode> list,String nodeid ,String rootid) throws IOException{

        String parentid = null;

        for(Iterator it = list.iterator(); it.hasNext();){
            MindNode mind = (MindNode) it.next();

            if(mind.getNodeId().equals(nodeid)){
                parentid = mind.getParentId();
            }
        }

        HashMap nodeList = new HashMap();
        MindNode2Util mindNode2Util = new MindNode2Util();
        Node2 root = null;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("name", "jsMind remote");
        meta.put("author", "hizzgdev@163.com");
        meta.put("version", "0.2");
        data.put("meta", meta);
        data.put("format", "node_tree");

        for (Iterator it = list.iterator(); it.hasNext();) {
            MindNode mindNode = (MindNode) it.next();
            Node2 node = new Node2();
            node.id = mindNode.getNodeId();
            node.topic = mindNode.getNodeName();
            node.parentid = mindNode.getParentId();
            node.color = mindNode.getColor().toString();
            nodeList.put(node.id, node);
        }

        Set entrySet = nodeList.entrySet();

        for (Iterator it = entrySet.iterator(); it.hasNext();) {
            Node2 node = (Node2) ((Map.Entry) it.next()).getValue();
            if ((node.parentid == null) || (node.parentid.equals(parentid))) {
                root = node;
            } else {

                try {
                    ((Node2) nodeList.get(node.parentid)).addChild(node);
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        }
        mindNode2Util.setState("1");

        data.put("data", root.toString());

        String datas = this.jsonAnalyze.object2Json(data).toString();

        System.out.println("datatatat:" + datas);
        datas = datas.replace("\"", "'");
        datas = datas.replace(" ", "");
        datas = datas.replace("'{", "{");
        datas = datas.replace("}'", "}");
        mindNode2Util.setDatas(datas);
        mindNode2Util.setKcmc(rootid);
        mindNode2Util.setMindJson2("success");
        return this.jsonAnalyze.object2Json(mindNode2Util);
    }
}
