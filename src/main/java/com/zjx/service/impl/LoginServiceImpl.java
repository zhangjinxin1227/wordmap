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
    public UserBean loginCheck2(String userName, String password){
        //int b = 0;
        UserBean user = userBeamMapper.getUserInfo(userName, password);
        /*if (user == null) {
            return b = 0; // 密码错误
        } else {
            return b = 1;   //学生端
        }*/
        return  user;
    }

    @Override
    public UserBean getUserByNickname(String name){
        return userBeamMapper.getUserByNickname(name);
    }

    @Override
    public Boolean saveUser(UserBean userBean){
        Integer i = userBeamMapper.saveUser(userBean);
        //注册用户时，新建用户积分信息
        Integer temp = userBeamMapper.insertToken(i);
        if(temp > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得城市信息
     * @return
     */
    @Override
    public List<City> getCityInfo(){
        return userBeamMapper.getCityInfo();
    }

    /**
     * 获得城市id
     * @param cityName
     * @return
     */
    @Override
    public Integer getCityName(String cityName){
        return userBeamMapper.getCityName(cityName);
    }
}
