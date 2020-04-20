package com.zjx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileMapper {

    //删除节点上对应的资源
    Integer deleteNodeFiles(@Param("nodeId") String nodeId);
}
