package com.zjx.model;

import lombok.Data;

@Data
public class Permissions {

    private Integer id;
    private String permissions;
    private Integer roleId;

    public Permissions(Integer id, String permissions, Integer roleId){
        this.id = id;
        this.permissions = permissions;
        this.roleId = roleId;
    }

}
