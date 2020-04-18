package com.zjx.service.impl;

import com.zjx.mapper.UserBeamMapper;
import com.zjx.model.City;
import com.zjx.model.UserBean;
import com.zjx.service.LoginService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private UserBeamMapper userBeamMapper;

    @Override
    public Integer loginCheck2(String userName, String password){
        int b = 0;
        UserBean user = userBeamMapper.getUserInfo(userName, password);
        if (user == null) {
            return b = 0; // 密码错误
        } else {
            return b = 1;   //学生端
        }
    }

    @Override
    public UserBean getUserByNickname(String name){
        return userBeamMapper.getUserByNickname(name);
    }

    @Override
    public Integer saveUser(UserBean userBean){
        return userBeamMapper.saveUser(userBean);
    }
    @Override
    public List<City> getCityInfo(){
        return userBeamMapper.getCityInfo();
    }
}
