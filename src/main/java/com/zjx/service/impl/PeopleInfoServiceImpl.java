package com.zjx.service.impl;

import com.zjx.mapper.TokenMapper;
import com.zjx.mapper.UserBeamMapper;
import com.zjx.model.Token;
import com.zjx.model.UserBean;
import com.zjx.service.PeopleInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PeopleInfoServiceImpl implements PeopleInfoService {

    @Resource
    private UserBeamMapper userBeamMapper;

    @Resource
    private TokenMapper tokenMapper;

    /**
     * 获得用户基本信息
     * @param userId
     * @return
     */
    @Override
    public UserBean getUserInfo(String userId){
        return userBeamMapper.getUserMsg(Integer.parseInt(userId));
    }

    /**
     * 获得用户token信息
     * @param userId
     * @return
     */
    @Override
    public Token getUsetToken(String userId){
        return tokenMapper.getUserToken(Integer.parseInt(userId));
    }
}
