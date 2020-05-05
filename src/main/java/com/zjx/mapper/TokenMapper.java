package com.zjx.mapper;

import com.zjx.model.Token;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TokenMapper {

    //根据用户id获得用户积分信息
    Token getUserToken(@Param("userId") Integer userId);

    //新建导图时增加一个积分
    void newMapToken(Token token);
}
