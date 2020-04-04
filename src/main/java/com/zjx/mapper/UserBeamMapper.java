package com.zjx.mapper;

import com.zjx.model.Permissions;
import com.zjx.model.Role;
import com.zjx.model.UserBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBeamMapper {

    //查询用户信息
    UserBean selectAllByName(String account);

    //根据用户id，查询用户角色
    List<Role> findRoleByUid(Integer id);

    //根据用户id查询用户权限
    List<Permissions>  findPermissionByUid(Integer id);

    //根据用户id 获得权限名称
    List<String> findRoleName(Integer id);

    //根据用户id 获得权限名称
    List<String> findPermissionsName(Integer id);

}
