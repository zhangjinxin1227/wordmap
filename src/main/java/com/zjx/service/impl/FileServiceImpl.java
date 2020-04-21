package com.zjx.service.impl;

import com.zjx.controller.MindMapController;
import com.zjx.mapper.FileMapper;
import com.zjx.model.UploadFile;
import com.zjx.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileMapper fileMapper;

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //删除节点上对应的资源
    @Override
    public Integer deleteNodeFiles(String nodeId){
        return fileMapper.deleteNodeFiles(nodeId);
    }

    //删除fileId对应的文件
    @Override
    public Integer deleteFileId(Integer fileId){
        return fileMapper.deleteFileId(fileId);
    }

    /**
     * 节点上传文件
     * @param uploadFile
     */
    @Override
    public void insertNodeFile(UploadFile uploadFile){
        Integer fileId = fileMapper.insertNodeFile(uploadFile);
        //fileMapper.insertNodeFileContact(fileId, uploadFile.getNodeId());
    }

    /**
     * 获得节点上的文件
     */
    @Override
    public List<UploadFile> getUploadeFile(String nodeId){
        return fileMapper.getUploadeFile(nodeId);
    }
}
