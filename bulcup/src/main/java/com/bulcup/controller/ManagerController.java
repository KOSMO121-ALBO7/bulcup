package com.bulcup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("manager")
public class ManagerController {
	
	@RequestMapping("/{step}")
	public String viewPage(@PathVariable String step) {
		return "/manager/"+step;
	}
}
