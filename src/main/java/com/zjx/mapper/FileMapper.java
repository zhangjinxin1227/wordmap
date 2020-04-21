package com.zjx.mapper;

import com.zjx.model.UploadFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper {

    //删除节点上对应的资源
    Integer deleteNodeFiles(@Param("nodeId") String nodeId);

    //删除fileId对应的文件
    Integer deleteFileId(@Param("fileId") Integer fileId);

    Integer insertNodeFile(UploadFile uploadFile);

    //void insertNodeFileContact(@Param("fileId")Integer fileId, @Param("nodeId") String nodeId);

    //获得某个节点上的文件
    List<UploadFile> getUploadeFile(@Param("nodeId") String nodeId);

    //获得用户上传的文件
    List<UploadFile> getUserUploadeFile(@Param("nodeId") String nodeId);
}
