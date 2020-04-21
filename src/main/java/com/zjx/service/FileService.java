package com.zjx.service;

import com.zjx.model.UploadFile;

import java.util.List;

public interface FileService {

    //删除节点上的所有文件
    Integer deleteNodeFiles(String nodeId);

    //删除fileId对应的文件
    Integer deleteFileId(Integer fileId);

    void insertNodeFile(UploadFile uploadFile);

    List<UploadFile> getUploadeFile(String nodeId);
}
