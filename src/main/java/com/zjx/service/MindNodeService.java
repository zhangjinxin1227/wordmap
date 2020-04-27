package com.zjx.service;

import com.zjx.model.MindNode;

import java.util.List;

public interface MindNodeService {

    Integer deleteNode(String nodeId);

    void insertNode(MindNode mindNode);

    void updateNodeParentId(String nodeId, String afterId);

    void setMapColor(String nodeId, String color);

    void updateNodeName(String nodeId, String nodeName);

    List<MindNode> getNope(List<MindNode> less, List<MindNode> more );

    Integer saveMapZsd(MindNode mindNode);

    MindNode getNodems(String nodeId);

}
