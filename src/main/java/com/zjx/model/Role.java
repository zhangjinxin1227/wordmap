package com.zjx.model;

import lombok.Data;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Set;

@Data
public class Role {

    private Integer id;
    private String roleName;
    private Integer userId;
    /**
     * 角色对应权限集合
     */
    private Set<Permissions> permissions;

    public Role(Integer id, String roleName, Integer userId, Set<Permissions> permissions){
        this.id = id;
        this.roleName = roleName;
        this.userId = userId;
        this.permissions = permissions;
    }
}
