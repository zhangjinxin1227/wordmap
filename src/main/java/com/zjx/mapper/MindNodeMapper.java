package com.zjx.mapper;

import com.zjx.model.MindNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MindNodeMapper {

    Integer deleteNode(@Param("nodeId") String nodeId);

    void insertNode(MindNode mindNode);

    void updateNodeName(@Param("nodeId") String nodeId, @Param("nodeName")String nodeName);

    Integer saveMapZsd(MindNode mindNode);

    MindNode getNodems(@Param("nodeId") String nodeId);

    void setMapColor(@Param("nodeId")String nodeId, @Param("color")String color);

    void updateNodeParentId(@Param("nodeId")String nodeId, @Param("afterId")String afterId);
}
