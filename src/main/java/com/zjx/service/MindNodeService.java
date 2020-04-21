package com.zjx.service;

import com.zjx.model.MindNode;

import java.util.List;

public interface MindNodeService {

    Integer deleteNode(String nodeId);

    List<MindNode> getNope(List<MindNode> less, List<MindNode> more );

    Integer saveMapZsd(MindNode mindNode);

    MindNode getNodems(String nodeId);

}
