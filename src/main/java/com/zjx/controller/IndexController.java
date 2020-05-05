package com.zjx.controller;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.TransparentBackgroundProducer;
import cn.apiclub.captcha.servlet.CaptchaServletUtil;
import cn.apiclub.captcha.text.renderer.DefaultWordRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/index")
public class IndexController {
	
	/**
	 * @author Ragty
	 * @serialData 2017.11.8
	 * 用来映射首页面
	 * @return
	 */
	@RequestMapping(value={"/","index.do"})
	public String index(HttpServletRequest request){
		//咱是先把用户信息放入session
		HttpSession session = request.getSession();
		session.setAttribute("userId","2");
		session.setAttribute("account","jinxin");
		session.setAttribute("userName","张金鑫");
		return "index";
	}


	/**
	 * 映射注册页面
	 * @return
	 */
	@RequestMapping(value="register.do")
	public String register(){
		return "register";
	}
	
	
	/**
	 *  映射登陆界面
	 * @return
	 */
	@RequestMapping(value="testLogin.do")
	public String testLogin(){
		return "testLogin";
	}
	
	
	/**
	 * 映射学生个人信息界面
	 * @return
	 */
    @RequestMapping(value="peopleInfo.do")
    public String stuinfo(){
    	return "stuinfo";
    }
    
    
    /**
     * 映射权限不足页面
     * @return
     */
    @RequestMapping({ "/access.do" })
	public String access() {
		return "access";
	}

    
    /**
     * 生成验证码
     * @param request
     * @param response
     */
	@RequestMapping({ "/getVerifyCode.do" })
	public void getVerifyMCode(Model model, HttpServletRequest request,
                               HttpServletResponse response) {
		
		//字体颜色
		List<Color> colors = new ArrayList();
		colors.add(Color.GREEN);
		colors.add(Color.BLUE);
		colors.add(Color.ORANGE);
		colors.add(Color.RED);

		//字体型号
		List<Font> fonts = new ArrayList<Font>();
		fonts.add(new Font("Geneva", 2, 32));
		fonts.add(new Font("Courier", 3, 32));
		fonts.add(new Font("Arial", 1, 32));
		
		DefaultWordRenderer wordRenderer = new DefaultWordRenderer(colors, fonts);// 验证码文本生成器
		
		Captcha captcha = new Captcha.Builder(150, 50).addText(wordRenderer)
				//.gimp(new DropShadowGimpyRenderer())
				.addBackground(new TransparentBackgroundProducer()).build();

		request.getSession().setAttribute("verifyCode", captcha.getAnswer());
		
		System.out.println("新版本验证码是@@@@@@"+captcha.getAnswer());
		
		CaptchaServletUtil.writeImage(response, captcha.getImage());
	}
}
