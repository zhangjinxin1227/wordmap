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
    public Integer deleteFileId(Integer fileId,Integer userId){
        return fileMapper.deleteFileId(fileId, userId);
    }

    /**
     * 节点上传文件
     * @param uploadFile
     */
    @Override
    public void insertNodeFile(UploadFile uploadFile){
        fileMapper.insertNodeFile(uploadFile);
        //fileMapper.insertNodeFileContact(fileId, uploadFile.getNodeId());
    }

    /**
     * 获得节点上的文件
     */
    @Override
    public List<UploadFile> getUploadeFile(String nodeId){
        List<UploadFile> list = fileMapper.getUploadeFile(nodeId);
        logger.info("数据库查到的文件列表是："+list);
        return list;
    }

    /**
     * 获得用户所有资源文件
     * @param userId 用户id
     * @return
     */
    @Override
    public List<UploadFile> getAllFile(Integer userId){
        List<UploadFile> list = fileMapper.getAllFile(userId);
        logger.info("获得本用户的文件总数是："+ list.size() + "个");
        return list;
    }
}
