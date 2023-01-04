package com.bulcup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bulcup.domain.ContactVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.service.ManagerService;
import com.bulcup.service.UserService;

@Controller
@RequestMapping("user")
public class UserController{

	@Autowired
	private UserService userService;
	
	@RequestMapping("insertContact.do")
	public String insertContact(ContactVO contactVO) {
		System.out.println(":INSert Contact");
		System.out.println("EMAU:L " + contactVO.getEmail());

		int result = userService.insertContact(contactVO);
		System.out.println("INSERTING CONTACT: " + result);
		return "redirect:index.html";
	}
	@GetMapping("/{step}")
	public String viewPage(@PathVariable String step) {
		return "/user/"+step;
	}
}
