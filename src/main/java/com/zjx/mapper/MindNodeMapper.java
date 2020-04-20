package com.zjx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MindNodeMapper {

    Integer deleteNode(@Param("nodeId") String nodeId);

    void deleteNodeFile(@Param("nodeId") String nodeId);
}
