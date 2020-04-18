package com.zjx.controller;

import com.zjx.json.JsonAnalyze;
import com.zjx.model.City;
import com.zjx.model.UserBean;
import com.zjx.service.LoginService;
import com.zjx.service.impl.ReturnStatus;
import com.zjx.service.impl.StatusMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/login")
public class LoginController {
	
	@Autowired
	private ReturnStatus returnStatus;
	@Autowired
	private JsonAnalyze jsonAnalyze;
	@Autowired
	private StatusMap statusMap;
	@Autowired
	private LoginService loginService;
	@Autowired
	private AuthenticationManager authenticationManager;

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	/**
	 * @author Ragty
	 * 用户登录验证接口
	 * @serialData 2018.3.5
	 * @param requestJsonBody
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/loginCheck2")
	@ResponseBody
	public String loginCheck2(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{
		
		Map<String, Object> map=jsonAnalyze.json2Map(requestJsonBody);
		String username = String.valueOf(map.get("user"));
		String password = String.valueOf(map.get("password"));
		String verifyImage = String.valueOf(map.get("verifyImage"));
		
	    String jsCode=String.valueOf(map.get("code"));

		if(verifyImage!="null") {
			
		   String resultVerifyCode = request.getSession().getAttribute("verifyCode").toString();//获取缓存生成的验证码
		//判断验证码是否正确，不正确的话返回一个错误的状态
		   if (!checkValidateCode(verifyImage, resultVerifyCode)) {
			  return returnStatus.verifyCodeError;
		    }
		}

		//判断登录信息(具体表现为是学生，管理员，用户名错误，密码错误)
		int isValidUser = loginService.loginCheck2(username, password);
		if (isValidUser == 1){
			
			HttpSession session2 = request.getSession();
			//在这里设置持续登录的时间
			session2.setAttribute("username", username);
			session2.setMaxInactiveInterval(6*60*60);
			
			//验证成功后将会把返回的Authentication对象存放在SecurityContext
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
			Authentication authentication = authenticationManager.authenticate(authRequest); //调用loadUserByUsername
			SecurityContextHolder.getContext().setAuthentication(authentication);

			//根据状态值返回
			return returnStatus.student;//学生端
		}else {
			return this.returnStatus.NotHaveUser;
		}

	}
	
	
	
	/**
	 *@author Ragty
	 * 验证用户是否登录
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/iflogin")
	@ResponseBody
	public String iflogin(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();
		Object nickname = session.getAttribute("username");
		map.put("nickname", nickname);
		map.put("status", Integer.valueOf(1));
		return this.jsonAnalyze.map2Json(map);
	}
	
	
	
	/**
	 * @author Ragty
	 *  退出登录操作
	 * @serialData 2018.3.5
	 * @param request
	 * @throws Exception
	 */
	@RequestMapping(value="/quitLogin.do")
	@ResponseBody
	public String quitLogin(HttpServletRequest request) throws Exception {
		HttpSession session = request.getSession();
		session.invalidate();
		return statusMap.a("1");
	}
	
	
	/**
	 * @author 张金鑫
	 *  用户注册接口（增加注册后即登录）
	 * @param requestJsonBody
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/register")
	@ResponseBody
	public String register(@RequestBody String requestJsonBody, HttpServletRequest request) throws IOException{
		
		Map<String, Object> map=jsonAnalyze.json2Map(requestJsonBody);
		logger.info("注册的用户信息是：" + map);
		String account = String.valueOf(map.get("account"));
		String password = String.valueOf(map.get("password"));
		String phone = String.valueOf(map.get("phone"));
		String email = String.valueOf(map.get("email"));
		String name=String.valueOf(map.get("name"));
		String verifyImage = String.valueOf(map.get("verifyImage"));
		String role = String.valueOf(map.get("role"));
		String resultVerifyCode = request.getSession()
				.getAttribute("verifyCode").toString();
		
		//判断输入的验证码是否和系统中的验证码相同
		if (!checkValidateCode(verifyImage, resultVerifyCode)) {
			 return statusMap.a("1");
		}
		
		//判断注册的用户名是否用过
		if (loginService.getUserByNickname(account) != null) {
			return statusMap.a("2");
		}
		
		//后台限制注册数据，使之不为空
		if(account.equals("") || password.equals("") || phone.equals("") || email.equals("")|| name.equals("") || verifyImage.equals("")){
           return statusMap.a("5");			
		}
		
		//将注册信息写入数据库
		UserBean userInfo = new UserBean();
		userInfo.setAccount(account);
		userInfo.setPassword(password);
		userInfo.setPhone(phone);
		userInfo.setEmail(email);
		userInfo.setName(name);
		
		HttpSession session = request.getSession();
		
		//保存注册信息并登录
		if (loginService.saveUser(userInfo) > 0) {
		//	System.out.println("成功保存注册信息");
		
			//验证成功后将会把返回的Authentication对象存放在SecurityContext
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(account, password);
			Authentication authentication = authenticationManager.authenticate(authRequest); //调用loadUserByUsername
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			session.setAttribute("username", account);
			session.setMaxInactiveInterval(6*60*60);
			return this.statusMap.a("3");
		}
		
		return this.statusMap.a("4");
		
	}

	/**
	 * @author 张金鑫
	 * 判断输入的验证码是否和系统生成的验证码相同
	 * @param verifyCode
	 * @param result_verifyCode
	 * @return
	 */
	protected boolean checkValidateCode(String verifyCode,
			String result_verifyCode) {
		if ((verifyCode == null)
				|| (!result_verifyCode.equalsIgnoreCase(verifyCode))) {
			return false;
		}
		return true;
	}

	@GetMapping(value="/getCityInfo")
	@ResponseBody
	public List<City> getCityInfo(){

		return loginService.getCityInfo();
	}
}
