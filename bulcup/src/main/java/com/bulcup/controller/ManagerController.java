package com.bulcup.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bulcup.Email;
import com.bulcup.domain.BlogVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.SubscribeVO;
import com.bulcup.service.ManagerService;

@Controller
@RequestMapping("manager")
public class ManagerController {

	@Autowired
	private ManagerService managerService;

	@Autowired
	private Email email;

	@PostMapping(value = "insertManager.do")
	public String insertManager(ManagerVO managerVO) {
		int result = managerService.insertManager(managerVO);
		if (result == 1)
			return "redirect:manager_registration_success.html";
		return "redirect:manager_registration_fail.html";
	}

	@GetMapping(value = "loginManager.do")
	public String loginManager(ManagerVO managerVO, HttpSession session) {
		ManagerVO result = managerService.selectManagerByIdAndPw(managerVO);

		if (result != null) {
			session.setAttribute("loginId", result.getId());
			session.setAttribute("loginEmail", result.getEmail());
			session.setAttribute("loginTel", result.getTel());
			session.setAttribute("loginPassword", result.getPassword());
			session.setAttribute("loginAuthority", result.getAuthority());
			return "redirect:manager_index.html";
		}
		return "redirect:manager_login.html";
	}

	@GetMapping(value = "logoutManager.do")
	public String logoutManager(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		session.invalidate();
		return "redirect:manager_logout.html";
	}

	@PostMapping(value = "updateManager.do")
	public String updateManager(ManagerVO managerVO, HttpServletRequest request, HttpSession session) {
		managerService.updateManager(managerVO);
		loginManager(managerVO, session); // 업데이트 된 정보로 세션값 재정의를 위해 로그인을 다시한다
		return "redirect:manager_account.html";
	}

	@PostMapping(value = "deleteManager.do")
	public String deleteManager(ManagerVO managerVO, HttpServletRequest request) {
		System.out.println("MANGER ID : " + managerVO.getId());
		int result = managerService.deleteManagerById(managerVO);
		if (result > 0) {
			logoutManager(request);
			return "redirect:manager_login.html";
		}
		return "redirect:manager_index.html";
	}

	@PostMapping(value = "updateManagerAccount.do")
	public String updateManager(ManagerVO managerVO) {
		System.out.println("controller:updateManagerAccount.do");
		System.out.println(managerVO.getAuthority());
		managerService.updateManager(managerVO);
		return "redirect:manager_manage.html";
	}

	@GetMapping(value = "deleteManagerAccount.do")
	public String deleteManagerAccount(String id) {
		System.out.println("controller:deleteManagerAccount.do");
		ManagerVO vo = new ManagerVO();
		vo.setId(id);
		managerService.deleteManagerById(vo);
		return "redirect:manager_manage.html";
	}

	@PostMapping(value = "updateContact.do")
	public String updateContact(ContactVO contactVO, HttpSession session) {
		System.out.println("controller: updateContact.do");
		contactVO.setManager_id(String.valueOf(session.getAttribute("loginId")));
		int result = managerService.updateContact(contactVO);
		if (result != 0) {
			HashMap<String, String> values = new HashMap<String, String>();
			values.put("contact", contactVO.getQuestion());
			values.put("answer", contactVO.getAnswer());
			email.Send("Bulcup 문의사항 답변입니다.", contactVO.getEmail(), "/mail-templates/contactTemplate", values);
		}
		return "redirect:manager_contact.html";
	}

	@GetMapping(value = "deleteContact.do")
	public String deleteContact(ContactVO vo) {
		managerService.deleteContact(vo);
		return "redirect:manager_contact.html";
	}

	@GetMapping(value = "manager_manage.html")
	public void manager_manage(Model m) {
		List<ManagerVO> list = managerService.getManagerList();
		m.addAttribute("getManagerList", list);
	}

	@GetMapping(value = "manager_blog.html")
	public void manager_blog(Model m) {
		System.out.println("Controller:: manager_blog");
		List<BlogVO> list = managerService.getBlogList();
		m.addAttribute("getBlogList", list);
		System.out.println("CONTROLLER :MANAGER_BLOG END");
	}

	@PostMapping(value = "insertBlog.do")
	public String insertBlog(BlogVO vo) {
		System.out.println("Controller:: insertBlog");
		int result = managerService.insertBlog(vo);
		System.out.println("Result is : " + result);
		return "redirect:manager_blog.html";
	}

	@PostMapping(value = "updateBlog.do")
	public String updateBlog(BlogVO vo) {
		System.out.println("Controller:: updateBlog");
		int a = managerService.updateBlog(vo);
		System.out.println("AAA: " + a);
		return "redirect:manager_blog.html";
	}

	@GetMapping(value = "deleteBlog.do")
	public String deleteBlog(BlogVO vo) {
		managerService.deleteBlog(vo);
		return "redirect:manager_blog.html";
	}

	@GetMapping(value = "manager_contact.html")
	public void manager_contact(Model m) {
		List<ContactVO> list = managerService.getContactList();
		m.addAttribute("getContactList", list);

		List<ContactVO> list2 = managerService.getFinishedContactList();
		m.addAttribute("getFinishedContactList", list2);
	}

	@GetMapping("sendSelfDiagnosis.do")
	public String sendSelfDiagnosis(HashMap<String, String> values) {
		values.put("name", "테스트 이름"); // 테스트
//		email.Send("Bulcup 자가진단 결과입니다.", values.get("email"), "selfDiagnosisTemplate", values);
		email.Send("테스트 타이틀", "cgtcsg@naver.com", "/mail-templates/selfDiagnosisTemplate", values);
		return "";
	}

	@Scheduled(cron = "0 0 1 1 * ?", zone = "Asia/Seoul")
	public void sendNews() {
		managerService.sendNews();
	}

	@GetMapping("/{step}")
	public String viewPage(@PathVariable String step) {
		return "manager/" + step;
	}
}
