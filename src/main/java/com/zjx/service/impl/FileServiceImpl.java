package com.zjx.service.impl;

import com.zjx.controller.MindMapController;
import com.zjx.mapper.FileMapper;
import com.zjx.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileMapper fileMapper;

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //删除节点上对应的资源
    public Integer deleteNodeFiles(String nodeId){
        return fileMapper.deleteNodeFiles(nodeId);
    }
}
