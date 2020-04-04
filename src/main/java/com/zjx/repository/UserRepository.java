package com.zjx.repository;

import com.zjx.entity.User;
import com.zjx.repository.base.BaseRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends BaseRepository<User> {
    //@Query(value = "select from User um_t_user where lst_flag='1' and  account = ?  ")
    //User findUserByAccount(@Param("account") String account);

    List<User> findUserByAccount(@Param("account") String account);
}
