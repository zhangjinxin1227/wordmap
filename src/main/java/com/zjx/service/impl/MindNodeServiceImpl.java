package com.zjx.service.impl;

import com.zjx.mapper.MindNodeMapper;
import com.zjx.model.MindNode;
import com.zjx.service.MindNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class MindNodeServiceImpl implements MindNodeService {

    @Resource
    private MindNodeMapper mindNodeMapper;

    //删除节点，并删除nodeFile表对应的数据
    @Resource
    public Integer deleteNode(String nodeId){
        Integer i = mindNodeMapper.deleteNode(nodeId);
        if(i > 0){
            mindNodeMapper.deleteNodeFile(nodeId);
        }
        return mindNodeMapper.deleteNode(nodeId);
    }

    /**
     * 获取删除后的节点(利用Set的不可重复性,Banner)  //利用list的contain也可去掉，所以我懒得写了
     * @param less
     * @param more
     * @param
     * @return
     */
    @Override
    public List<MindNode> getNope(List<MindNode> less, List<MindNode> more ){

        Set<MindNode> les = new HashSet<MindNode>(less);
        Set<MindNode> mor = new HashSet<MindNode>(more);
        List<MindNode> target = new ArrayList<MindNode>();
        Iterator<MindNode> it = mor.iterator();
        while (it.hasNext()) {
            MindNode mind = it.next();
            if( les.add(mind) ){
                target.add(mind);
            }
        }
        return target;
    }

    /**
     * 保存知识点描述
     * @param mindNode
     * @return
     */
    @Override
    public Integer saveMapZsd(MindNode mindNode){
        return mindNodeMapper.saveMapZsd(mindNode);
    }

    /**
     * 保存知识点描述
     * @param nodeId
     * @return
     */
    @Override
    public MindNode getNodems(String nodeId){
        return mindNodeMapper.getNodems(nodeId);
    }
}
