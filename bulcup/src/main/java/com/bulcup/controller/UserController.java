package com.bulcup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("user")
public class UserController {
	
	@RequestMapping("/{step}")
	public String viewPage(@PathVariable String step) {
		return "/user/"+step;
	}
}
