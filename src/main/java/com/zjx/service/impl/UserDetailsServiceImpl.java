package com.zjx.service.impl;

import com.zjx.model.UserBean;
import com.zjx.entity.WebUserDetails;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Resource
	private LoginServiceImpl loginService;
	
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	//登录验证
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//这里应该可以不用再查了
		UserBean userBean = loginService.getUserByNickname(username);
		
		//读取当前用户有哪些角色权限
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("Role_User");

		authorities.add(authority);
		
		
		boolean enables = true;
		//System.out.println("username="+username+" password="+users.getPassword()+" enables="+enables);//+" authorities"+authorities
		WebUserDetails webUserDetails = new WebUserDetails(username, userBean.getPassword(), enables, authorities);
		
		return webUserDetails;
		
	}
	

}
