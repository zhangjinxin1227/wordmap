package com.zjx.service.impl;


import com.zjx.entity.Role;
import com.zjx.repository.RoleRepository;
import com.zjx.service.RoleService;
import com.zjx.vo.ResponseVO;
import com.zjx.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleRepository roleRepository;

    @Override
    public ResponseVO findAllRoleVO() {
        List<Role> rolePOList = roleRepository.findAll();
        List<RoleVO> roleVOList = new ArrayList<>();
        rolePOList.forEach(rolePO->{
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(rolePO,roleVO);
            roleVOList.add(roleVO);
        });
        return ResponseVO.success(roleVOList);
    }

    @Override
    public Role findById(Integer id) {
        return roleRepository.findById(id).get();
    }
}
