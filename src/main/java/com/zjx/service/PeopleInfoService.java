package com.zjx.service;

import com.zjx.model.Token;
import com.zjx.model.UserBean;

public interface PeopleInfoService {

    UserBean getUserInfo(String userId);

    Token getUsetToken(String userId);
}
