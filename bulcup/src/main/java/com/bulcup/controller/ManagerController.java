package com.bulcup.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bulcup.Email;
import com.bulcup.MyLogger;
import com.bulcup.paging;
import com.bulcup.domain.BlogVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.PaginationVO;
import com.bulcup.domain.QuestionVO;
import com.bulcup.service.ManagerService;

@Controller
@RequestMapping("manager")
public class ManagerController {

	@Autowired
	private ManagerService managerService;

	@Autowired
	private Email email;

	MyLogger logger = MyLogger.getLogger();


	@GetMapping("/{step}")
	public String viewPage(@PathVariable String step) {
		logger.log("Manager page: " + step);
		return "manager/" + step;
	}

	@PostMapping(value = "insertManager.do")
	public String insertManager(ManagerVO managerVO) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();        
		String password =  managerVO.getPassword();
		String encPass = encoder.encode(password);   
		managerVO.setPassword(encPass);

		int result = managerService.insertManager(managerVO);
		if (result == 1) {
			logger.log("Manager page: " + "insertManager.do");	
			return "redirect:manager_registration_success.html";
		}
		logger.warning("Manager page: " + "insertManager.do");	
		return "redirect:manager_registration_fail.html";
	}

	@GetMapping(value = "loginManager.do")
	public String loginManager(ManagerVO managerVO, HttpSession session) {
		ManagerVO result = managerService.selectManagerByIdAndPw(managerVO);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); 
		boolean check = false;
		if(result != null) {
			if(encoder.matches(managerVO.getPassword(), result.getPassword())) {
				check = true;
			} 
		}
		if( check == true) {
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
		logger.log("Manager page: " + "logoutManager.do");
		return "redirect:manager_logout.html";
	}

	@PostMapping(value = "updateManager.do")
	public String updateManager(ManagerVO managerVO, HttpServletRequest request, HttpSession session) {
		managerService.updateManager(managerVO);
		loginManager(managerVO, session); // 업데이트 된 정보로 세션값 재정의를 위해 로그인을 다시한다
		logger.log("Manager page: " + "logoutManager.do");
		return "redirect:manager_account.html";
	}

	@PostMapping(value = "deleteManager.do")
	public String deleteManager(ManagerVO managerVO, HttpServletRequest request) {
		System.out.println("MANGER ID : " + managerVO.getId());
		int result = managerService.deleteManagerById(managerVO);
		if (result > 0) {
			logoutManager(request);
			logger.warning("Manager page: " + "deleteManager.do");
			return "redirect:manager_login.html";
		}
		logger.log("Manager page: " + "deleteManager.do");
		return "redirect:manager_index.html";
	}

	@PostMapping(value = "updateManagerAccount.do")
	public String updateManager(ManagerVO managerVO) {
		System.out.println("controller:updateManagerAccount.do");
		System.out.println(managerVO.getAuthority());
		managerService.updateManager(managerVO);
		logger.log("Manager page: " + "updateManagerAccount.do");
		return "redirect:manager_manage.html";
	}

	@GetMapping(value = "deleteManagerAccount.do")
	public String deleteManagerAccount(String id) {
		System.out.println("controller:deleteManagerAccount.do");
		ManagerVO vo = new ManagerVO();
		vo.setId(id);
		managerService.deleteManagerById(vo);
		logger.log("Manager page: " + "deleteManagerAccount.do");
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
		logger.log("Manager page: " + "updateContact.do");
		return "redirect:manager_contact.html";
	}

	@GetMapping(value = "deleteContact.do")
	public String deleteContact(ContactVO vo) {
		managerService.deleteContact(vo);
		logger.log("Manager page: " + "deleteContact.do");
		return "redirect:manager_contact.html";
	}

	@GetMapping(value = "manager_manage.html")
	public void manager_manage(Model m, @RequestParam(value = "pageNo", required = false) String pageNo) {
		if(pageNo == null) pageNo = "1";
		int totalRecord = managerService.managerCount();
		PaginationVO pageVO = new PaginationVO(Integer.parseInt(pageNo), totalRecord, 10, 5);
		List<ManagerVO> pglist = managerService.getManagerListPg(pageVO);
		m.addAttribute("pageVO", pageVO);
		m.addAttribute("getManagerListPg", pglist);

		logger.log("Manager page: " + "manager_manage.html");
	}

	@GetMapping(value = "manager_blog.html")
	public void manager_blog(Model m, @RequestParam(value = "pageNo", required = false) String pageno) {
		if (pageno != null)
			paging.pageNo = Integer.valueOf(pageno);
		else paging.pageNo = 1;
		paging.getPageSize = paging.countLines();
		paging.totalPageNo = paging.getPageSize / paging.pageSize + 1;
		System.out.println(paging.getPageSize + "AAA " + paging.totalPageNo);
		System.out.println("Controller:: manager_blog");
		List<BlogVO> list = managerService.getBlogList();
		m.addAttribute("getBlogList", list);
		m.addAttribute("pageNo", paging.pageNo);
		m.addAttribute("totalPageNo", paging.totalPageNo);
		System.out.println("CONTROLLER :MANAGER_BLOG END");
		logger.log("Manager page: " + "manager_blog.html");
	}

	@PostMapping(value = "insertBlog.do")
	public String insertBlog(BlogVO vo) {
		System.out.println("Controller:: insertBlog");
		int result = managerService.insertBlog(vo);
		System.out.println("Result is : " + result);
		logger.log("Manager page: " + "insertBlog.do");
		return "redirect:manager_blog.html";
	}

	@PostMapping(value = "updateBlog.do")
	public String updateBlog(BlogVO vo) {
		System.out.println("Controller:: updateBlog");
		int a = managerService.updateBlog(vo);
		System.out.println("updateBlog: " + a);
		logger.log("Manager page: " + "updateBlog.do");
		return "redirect:manager_blog.html";
	}

	@GetMapping(value = "deleteBlog.do")
	public String deleteBlog(BlogVO vo) {
		managerService.deleteBlog(vo);
		logger.log("Manager page: " + "deleteBlog.do");
		return "redirect:manager_blog.html";
	}

	// 문의 사항 응답 대기
	@GetMapping(value = "manager_contact.html")
	public void manager_contact(Model m) {
		List<ContactVO> list = managerService.getContactList();
		m.addAttribute("getContactList", list);
	}

	// 문의 사항 응답 완료
	@GetMapping(value = "manager_contactComplete.html")
	public void manager_contactComplete(Model m) {
		List<ContactVO> list2 = managerService.getFinishedContactList();
		m.addAttribute("getFinishedContactList", list2);
		logger.log("Manager page: " + "manager_contact.html");
	}

	//	@PostMapping("sendSelfDiagnosis.do")
	//	public String sendSelfDiagnosis(HashMap<String, String> values) {
	//		values.put("name", "테스트 이름"); // 테스트
	//		//		email.Send("Bulcup 자가진단 결과입니다.", values.get("email"), "selfDiagnosisTemplate", values);
	//		email.Send("테스트 타이틀", "cgtcsg@naver.com", "/mail-templates/selfDiagnosisTemplate", values);
	//		logger.log("Manager page: " + "sendSelfDiagnosis.do");
	//		return "";
	//	}

	@GetMapping(value = "manager_nutrient.html")
	public void manager_nutrient(Model m, @RequestParam(value = "pageNo", required = false) String pageNo) {
		if(pageNo == null) pageNo = "1";
		Integer totalRecord = managerService.foodCount();
		PaginationVO pageVO = new PaginationVO(Integer.parseInt(pageNo), totalRecord, 10, 5);
		List<FunctionalFoodVO> pglist = managerService.getFunctionalFoodListPg(pageVO);
		m.addAttribute("pageVO", pageVO);
		m.addAttribute("getFunctionalFoodListPg", pglist);

		logger.log("Manager page: " + "manager_nutrient.html");
	}

	@PostMapping(value = "updateFunctionalFood.do")
	public String updateFunctionalFood(FunctionalFoodVO vo) {
		System.out.println("controller : updateFunctionalFood.do");
		managerService.updateFunctionalFood(vo);
		logger.log("Manager page: " + "updateFunctionalFood.do");
		return "redirect:manager_nutrient.html";
	}

	// 영양제 정보 삭제
	@GetMapping("deleteFunctionalFood.do")
	public String deleteFunction(FunctionalFoodVO vo) {
		managerService.deleteFuctionalFood(vo);
		logger.log("Manager page: " + "deleteFunctionalFood.do");
		return "redirect:manager_nutrient.html";
	}

	@GetMapping(value = "manager_question.html")
	public String manager_question(Model m, @RequestParam(value = "pageNo", required = false) String pageNo) {
		if(pageNo == null) pageNo = "1";
		int totalRecord = managerService.questionCount();
		PaginationVO pageVO = new PaginationVO(Integer.parseInt(pageNo), totalRecord, 10, 5);
		List<QuestionVO> pglist = managerService.getQuestionListPg(pageVO);
		m.addAttribute("pageVO", pageVO);
		m.addAttribute("getQuestionListPg", pglist);
		return "manager/manager_question.html";
	}

	@GetMapping(value = "deleteQuestion.do")
	public String deleteQuestion(QuestionVO vo) {
		managerService.deleteQuetstion(vo);
		System.out.println("딜리트");
		return "redirect:manager_question.html";
	}

	@PostMapping(value = "updateQuestion.do")
	public String updateQuestion(QuestionVO vo) {
		managerService.updateQuestion(vo);
		System.out.println("업데이트");
		return "redirect:manager_question.html";
	}

	@PostMapping(value = "insertQuestion.do")
	public String insertQuestion(QuestionVO vo) {
		managerService.insertQuestion(vo);
		return "redirect:manager_question.html";
	}

	@GetMapping(value = "getCoordinates")
	@ResponseBody
	public String[] getCoordinates() {
		System.out.println("GETTING COORD?");
		Scanner sc, sc2;
		try {
			sc = new Scanner(new File("log/logCoord/logxcoord.log"));
			sc2 = new Scanner(new File("log/logCoord/logycoord.log"));

			String[] array = null;
			String[] array2 = null;
			while (sc.hasNextLine()) {
				String s = sc.nextLine();
				String s2 = sc2.nextLine();
				array = s.split("-");
				array2 = s2.split("-");
			} // while
			sc.close();
			sc2.close();
			String[] array3 = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				array3[i] = array[i] + ", " + array2[i];
			} // [(x, y),(x, y),(x, y),(x, y)...]
			return array3;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping(value = "manager_chart_site.html")
	public String getCountPage2(Model m) {
		try {
			// 바이트 단위로 파일읽기
			String filePath1 = "log/pages/pageindex.log"; // 대상 파일
			String filePath2= "log/pages/pagecontact.log";
			String filePath3 = "log/pages/pagesearch.log";
			String filePath4 = "log/pages/pageselfDiagnosisStart.log";
			String filePath5 = "log/time/timeindex.log"; // 대상 파일
			String filePath6= "log/time/timecontact.log";
			String filePath7 = "log/time/timesearch.log";
			String filePath8 = "log/time/timeselfDiagnosisStart.log";

			String indexCount= getReadForGetCountPage(filePath1);
			String contactCount= getReadForGetCountPage(filePath2);
			String searchCount= getReadForGetCountPage(filePath3);
			String selfCount= getReadForGetCountPage(filePath4);
			String index= getReadForGetCountPage(filePath5);
			String contact= getReadForGetCountPage(filePath6);
			String search= getReadForGetCountPage(filePath7);
			String self= getReadForGetCountPage(filePath8);

			m.addAttribute("indexCount",indexCount);
			m.addAttribute("contactCount", contactCount);
			m.addAttribute("searchCount",searchCount);
			m.addAttribute("selfCount",selfCount);
			m.addAttribute("self", Integer.parseInt(self));
			m.addAttribute("index",Integer.parseInt(index));
			m.addAttribute("contact", Integer.parseInt(contact));
			m.addAttribute("search",Integer.parseInt(search));

			return "manager/manager_chart_site.html";
			
		} catch (Exception e) {
			e.getStackTrace();
		}
		return "manager/manager_chart_site.html";
	}

	@Scheduled(cron = "0 0 1 1 * ?", zone = "Asia/Seoul")
	public void sendNews() {
		managerService.sendNews();
		logger.log("Manager page: " + "sendNews");
	}

	public String getReadForGetCountPage(String filepath) {
		try {
			Scanner sc = new Scanner(new File(filepath));
			if(sc.hasNext()) {
				return sc.nextLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

}
