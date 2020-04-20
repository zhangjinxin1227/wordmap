package com.zjx.service;

import com.zjx.model.MindMap;
import com.zjx.model.MindNode;

import java.io.IOException;
import java.util.List;

public interface MindMapService {

    Boolean saveMindMap(MindMap mindMap);

    MindMap getMindMap(String rootId);

    String  openMind(List<MindNode> list, String type) throws IOException;

    Boolean updateMindMap(MindMap mindMap);

    List<MindNode> getChild(List<MindNode> list, String nodeid, List<MindNode> storage);

    Integer deleteMap(String rootId);

    List<MindMap> getMapList(String userId);

    Integer shareMap(String rootId);

    Integer deleteshareMap(String rootId);

    List<MindMap> getShareMapList(String userId);
}
