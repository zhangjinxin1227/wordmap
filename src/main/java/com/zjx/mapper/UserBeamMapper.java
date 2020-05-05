package com.zjx.mapper;

import com.zjx.model.City;
import com.zjx.model.UserBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBeamMapper {

    //根据用户账号和密码获得用户信息
    UserBean getUserInfo(@Param("account") String userName, @Param("password") String password);
    //判断用户名是否注册成功
    UserBean getUserByNickname(@Param("name") String name);
    //判断用户注册是否成功
    Integer saveUser(UserBean userBean);

    Integer insertToken(@Param("userId") Integer userId);

    List<City> getCityInfo();

    Integer getCityName(@Param("cityName")String cityName);

}
