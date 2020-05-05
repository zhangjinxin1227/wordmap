package com.zjx.service;

import com.zjx.model.City;
import com.zjx.model.UserBean;

import java.util.List;

public interface LoginService {

    Integer loginCheck2(String userName, String password);

    UserBean getUserByNickname(String name);

    Boolean saveUser(UserBean userBean);

    List<City> getCityInfo();

    Integer getCityName(String cityName);


}
