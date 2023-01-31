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

import com.bulcup.EmailSender;
import com.bulcup.MyLogger;
import com.bulcup.paging;
import com.bulcup.domain.BlogVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.PaginationVO;
import com.bulcup.domain.QuestionVO;
import com.bulcup.domain.NoticeVO;
import com.bulcup.service.ManagerService;

@Controller
@RequestMapping("manager")
public class ManagerController {

	/* MVC 패턴 중 Controller에서 발생하는 Request(이하 요청)에 따라, 이를 수행할 수 있는 서비스를 호출하기 위해, Service를 변수로 설정해주고,
	 * 이를 Spring이 자동으로 메모리에 온 로딩 할 수 있도록 AutoWired Annotation(이하 어노테이션)을 부착함. */
	@Autowired
	private ManagerService managerService;		

	/*이메일을 자동으로 전송하기 위해, Email Class를 별도로 생성해, 이를 호출하였음. 메모리에 자동으로 온 로딩하고자 Autowired 어노테이션 부착 */
	@Autowired
	private EmailSender email;

	/* 로그 처리 관련 Class , 이하 주석은 지선 Plz... */
	MyLogger logger = MyLogger.getLogger();

	/*********************************************************
	 * 함수명 : viewPage
	 * 역할 : 사용자가 특정 페이지를 선택 (클릭) 했을 때 해당 페이지로 이동함
	 * @param step (사용자가 선택한 페이지)
	 * @return : 사용자가 선택한 페이지로 이동됨 (이때 관리자 페이지 중 해당 페이지로 이동하게끔 설정함.)
	 */
	@GetMapping("/{step}")
	public String viewPage(@PathVariable String step) {
		logger.log("Manager page: " + step);
		return "manager/" + step;
	}

	/*********************************************************
	 * 함수명 : InsertManager
	 * 역할 : DataBase(이하 DB)에 관리자 계정 및 추가 정보 등록 
	 * 이후 입력한 비밀번호는 Spring Security를 활용해 복호화 (난수를 활용해, 알아볼 수 없도록 만드든 것.) 처리함.
	 * @param managerVO
	 * @return 계정이 등록된 경우 1, 실패한 경우 0 이 돌아옴. 이 때를 조건을 설정해, 설정한 페이지로 리다이렉팅(창을 새로고침하고 지정된 페이지로 이동하게 함).
	 */
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

	/*********************************************************
	 * 함수명 : LoginManager
	 * 역할 : 관리자가 사이트에 로그인 하기 위해 입력한 정보(ID,PW)를 VO에 담아 DB와 비교해, 일치하는 경우 다른 VO에 담겨 돌아오고, 아닌 경우 빈 VO로 돌아옴.
	 * Spring Security 를 사용해 비밀번호가 복호화 되어 있으므로, 이를 다시 비교하기 위해 Encoder를 재호출 해, VO에 담겨있는 비밀번호들을 복호화해서 서로 비교함.
	 * @param managerVO
	 * @param session
	 * @return VO(매니저)로 돌아오고, 복호화된 비밀번호를 서로 비교했을 때 일치하는 경우, VO에 있는 필요한 정보들을 Session(세션)에 저장함.
	 */
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

	/*********************************************************
	 * 함수명 : logoutManager
	 * 역할 : 관리자가 작업을 마무리하고 사이트에서 로그아웃을 하고자 할 때, session에 있는 모든 정보를 초기화 함.
	 * @param request
	 * @return 세션 초기화
	 */
	@GetMapping(value = "logoutManager.do")
	public String logoutManager(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		session.invalidate();
		logger.log("Manager page: " + "logoutManager.do");
		return "redirect:manager_logout.html";
	}

	/*********************************************************
	 * 함수명 : updateManager
	 * 역할 : 관리자의 계정 정보를 수정할 때, 입력된 수정사항을 VO에 담아, 데이터베이스를 조회해 일치하는 ID의 정보를 수정함.
	 * @param managerVO
	 * @param request
	 * @param session
	 * @return 수정에 성공한 경우 1이 돌아오고 , 실패한 경우 0 이 리턴됨. (이는 해당하는 특정 행에서 이뤄지는 작업이고 컴퓨터 언어의 기본은 0 그리고 1)
	 * 이는 DML (Data Manifulation Language/ 이하 데이터 조작 언어) 중 C,U,D (Create(Insert),Update(Modify),Delete 모두 해당됨)
	 */
	@PostMapping(value = "updateManager.do")
	public String updateManager(ManagerVO managerVO, HttpServletRequest request, HttpSession session) {
		managerService.updateManager(managerVO);
		loginManager(managerVO, session); // 업데이트 된 정보로 세션값 재정의를 위해 로그인을 다시한다
		logger.log("Manager page: " + "logoutManager.do");
		return "redirect:manager_account.html";
	}

	/*********************************************************
	 * 함수명 : DeleteManager
	 * 역할 : 특정 관리자의 계정 정보를 데이터베이스에서 삭제함.
	 * @param managerVO
	 * @param request
	 * @return 삭제에 성공한 경우 1이, 아닌 경우 0이 리턴됨.
	 * 리턴 결과가 1인 경우, 다시 로그인 페이지로 이동되게끔 리다이렉팅을 설정함. 0일 경우 관리자 메인 페이지로 이동함.
	 */

	@PostMapping(value = "deleteManager.do")
	public String deleteManager(ManagerVO managerVO, HttpServletRequest request) {
		System.out.println("MANGER ID : " + managerVO.getId());
		int result = managerService.deleteManagerById(managerVO);
		if (result > 0) {
			logoutManager(request);		// 현재 로그인 된 계정이 세션에 남아있으므로 세션 초기화를 실행한다.
			logger.warning("Manager page: " + "deleteManager.do");
			return "redirect:manager_login.html";
		}
		logger.log("Manager page: " + "deleteManager.do");
		return "redirect:manager_index.html";
	}

	/*********************************************************
	 * 함수명 : UpdateManagerAccount (관리자 관리 페이지에서 작동함.)
	 * 역할 : 모달에서 관리자 계정 정보 수정사항을 모두 입력하고, 수정하기 버튼을 선택했을 때 해당 정보를 VO에 담아 DB로 보내, 해당하는 계정의 정보를 수정함.
	 * @param managerVO
	 * @return 성공하면 1, 아닌 경우 0 이 리턴됨.
	 */
	@PostMapping(value = "updateManagerAccount.do")
	public String updateManager(ManagerVO managerVO) {
		System.out.println("controller:updateManagerAccount.do");
		System.out.println(managerVO.getAuthority());
		managerService.updateManager(managerVO);
		logger.log("Manager page: " + "updateManagerAccount.do");
		return "redirect:manager_manage.html";
	}

	/*********************************************************
	 * 함수명 : DeleteManagerAccount (관리자 관리 페이지에서 작동함.)
	 * 역할 : 계정정보 삭제하기 버튼을 선택했을 때, Id를 VO에 담아 DB로 보내, 해당 계정 정보를 삭제함.
	 * @param id
	 * @return 성공하면 1, 아닌 경우 0 이 리턴됨.
	 * 이후 현재 페이지로 리다이렉팅 (작업이 진행된 결과가 바로 반영될 수 있도록)
	 */
	@GetMapping(value = "deleteManagerAccount.do")
	public String deleteManagerAccount(String id) {
		System.out.println("controller:deleteManagerAccount.do");
		ManagerVO vo = new ManagerVO();
		vo.setId(id);
		managerService.deleteManagerById(vo);
		logger.log("Manager page: " + "deleteManagerAccount.do");
		return "redirect:manager_manage.html";
	}

	/*********************************************************
	 * 함수명 : UpdateContact
	 * 역할 : 현재 접속해 있는 관리자가, 문의사항을 확인하고 답변을 등록했을 때, 답변한 관리자의 ID와 답변내용을 VO에 담아, 이를 DB에 반영하도록 함. 
	 * @param contactVO
	 * @param session
	 * @return 답변 등록에 성공한 경우 (업데이트에 성공한 경우) 1, 아닌 경우 0이 돌아옴.
	 * 수정 사항이 즉시 반영되도록 현재 페이지로 리다이렉팅
	 */
	@PostMapping(value = "updateContact.do")
	public String updateContact(ContactVO contactVO, HttpSession session) {
		System.out.println("controller: updateContact.do");
		contactVO.setManager_id(String.valueOf(session.getAttribute("loginId")));
		int result = managerService.updateContact(contactVO);
		if (result != 0) {
			String question = contactVO.getQuestion();
			contactVO.setQuestion(question.substring(question.indexOf(",") + 1));
			HashMap<String, String> values = new HashMap<String, String>();
			values.put("contact", contactVO.getQuestion());
			values.put("answer", contactVO.getAnswer());
			email.Send("Bulcup 문의사항 답변입니다.", contactVO.getEmail(), "/mail-templates/contactTemplate", values);
		}
		logger.log("Manager page: " + "updateContact.do");
		return "redirect:manager_contact.html";
	}

	/*********************************************************
	 * 함수명 : DeleteContact
	 * 역할 : 관리자가 문의사항을 삭제할 수 있도록 함. (문의 사항이 사이트 이용과 관련되어 있지 않거나, 같은 이메일로 반복해서 동일한 내용의 질문이 들어오는 경우를 대비)
	 * @param vo
	 * @return 삭제에 성공한 경우 1, 아닌 경우 0 이 리턴됨.
	 * 삭제 결과가 즉시 반영되도록 현재 페이지로 리다이렉팅.
	 */
	@GetMapping(value = "deleteContact.do")
	public String deleteContact(ContactVO vo) {
		managerService.deleteContact(vo);
		logger.log("Manager page: " + "deleteContact.do");
		return "redirect:manager_contact.html";
	}

	/*********************************************************
	 * 함수명 : Manager_manage (Paging)
	 * 역할 : 관리자가 한번에 모든 정보를 조회하면, 사이트 이용의 편의성이 떨어지므로, 이를 방지하기 위해 한번에 출력가능한 관리자 정보의 수를 조정해, 편의성을 향상시킴.
	 * @param m (Model, View에 처리된 결과를 담아 출력할 수 있도록 하는 요소)
	 * @param pageNo
	 * 모델에 페이징과 관련된 VO와 페이징 처리된 List를 담아 이를 View Page에 전달함.
	 */

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

	/*********************************************************
	 * 함수명 : Manager_blog (With Paging)
	 * 역할 : 크롤링 된 뉴스 정보가 담겨 있는 파일을 읽어들여, 이를 영양 상식 관리 페이지에 출력함. 한번에 모든 정보가 출력되는 것을 방지하고자, 출력되는 정보의 수를 제한해 편의성을 향상시킴.
	 * @param m(Model)
	 * @param pageno
	 * 모델에 파일에서 읽어들인 결과가 담겨있는 VO와 , PageNo (출력 시작점), TotalPageNO (총 출력할 갯수) 를 담아 View Page에 전달함.
	 */
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

	/*********************************************************
	 * 함수명 : InsertBlog
	 * 역할 : 크롤링 된 뉴스 정보들을 별도의 파일에 관리하는데,새로이 크롤링한 뉴스 중 필요한 정보를 VO에 담아, 이를 ToAppend라는 변수에 재할당 하고, 이를 파일에 추가적으로 기록함.
	 * @param vo
	 * @return 파일 기록에 성공하는 경우 1, 아닌 경우 0 이 돌아옴.
	 */
	@PostMapping(value = "insertBlog.do")
	public String insertBlog(BlogVO vo) {
		System.out.println("Controller:: insertBlog");
		int result = managerService.insertBlog(vo);
		System.out.println("Result is : " + result);
		logger.log("Manager page: " + "insertBlog.do");
		return "redirect:manager_blog.html";
	}

	/*********************************************************
	 * 함수명 : UpdateBlog
	 * 역할 : 크롤링 된 뉴스 정보가 담겨있는 파일을 새로운 버전으로 수정할 수 있음. (이 때 기존의 정보를 모두 삭제하고 새로운 정보들이 입력됨.)
	 * @param vo
	 * @return 파일 수정에 성공하는 경우 1, 아닌 경우 0 이 돌아옴. (기존 파일 삭제 성공 시 1, 신규 정보 등록 성공 시 1 = 이 둘의 합이 2인 경우 1이 돌아오게끔 설정함.)
	 */
	@PostMapping(value = "updateBlog.do")
	public String updateBlog(BlogVO vo) {
		System.out.println("Controller:: updateBlog");
		int a = managerService.updateBlog(vo);
		System.out.println("updateBlog: " + a);
		logger.log("Manager page: " + "updateBlog.do");
		return "redirect:manager_blog.html";
	}

	/*********************************************************
	 * 함수명 : DeleteBlog
	 * 역할 : 크롤링된 뉴스 정보가 모여있는 파일과 이와 동일한 Swap파일(파일 처리를 위한 임시 파일)을 생성해, 삭제하고자 하는 url을 VO에 담아 보내고, 
	 * 		 이와 일치하는 url이 파일에 존재하는 경우, 파일에서 해당 url을 삭제함.
	 * @param vo
	 * @return 삭제에 성공하는 경우 1, 아닌 경우 0 이 돌아옴.
	 */
	@GetMapping(value = "deleteBlog.do")
	public String deleteBlog(BlogVO vo) {
		managerService.deleteBlog(vo);
		logger.log("Manager page: " + "deleteBlog.do");
		return "redirect:manager_blog.html";
	}

	/*********************************************************
	 * 함수명 : Manager_Contact
	 * 역할 : 아직 답변이 등록되지 않은 문의사항을 DB에서 조회해 가져오고 , 이를 모델에 담아 View Page로 전달함.
	 * @param m(Model)	 
	 */
	@GetMapping(value = "manager_contact.html")
	public void manager_contact(Model m, @RequestParam(value="pageNo", required=false)String pageNo) {
		if(pageNo==null)pageNo="1";
		Integer totalrecod=managerService.waitCount();
		PaginationVO pageVo = new PaginationVO(Integer.parseInt(pageNo), totalrecod, 10, 5);
		List<ContactVO> list = managerService.getContactList(pageVo);
		m.addAttribute("pageVO",pageVo);
		m.addAttribute("getContactList", list);
	}

	/*********************************************************
	 * 함수명 : manager_contactComplete
	 * 역할 : 관리자가 답변을 등록한 문의사항을 DB에서 조회해 가져오고, 모델에 담아 View Page로 전달함.
	 * @param m(Model) 
	 */
	@GetMapping(value = "manager_contactComplete.html")
	public void manager_contactComplete(Model m,@RequestParam(value = "pageNo", required = false) String pageNo) {
		if(pageNo == null)pageNo = "1";
		Integer totalrecord = managerService.completeCount();
		PaginationVO pageVo = new PaginationVO(Integer.parseInt(pageNo) , totalrecord, 10,5);
		List<ContactVO> list2 = managerService.getFinishedContactList(pageVo);
		m.addAttribute("pageVO",pageVo);
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

	/*********************************************************
	 * 함수명 : manager_nutrient
	 * 역할 : DB에 입력되어 있는 모든 영양제 정보를 가져오되, 한번에 모든 정보를 가져오는 경우 사이트의 편의성이 하락하므로, 한번에 조회되는 숫자를 제한해 편의성을 향상시킴.
	 * 		 조회된 VO와 List를 모델에 담아, 이를 View Page로 가져감.
	 * @param m
	 * @param pageNo
	 */
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

	/*********************************************************
	 * 함수명 : updateFunctionalFood
	 * 역할 : 관리자가 수정한 영양제 정보를 VO에 담아서 가져가며, DB를 조회해 일치하는 ID에 업데이트함. 
	 * @param vo
	 * @return 수정이 반영되면 1, 아닌 경우 0으로 돌아옴. 수정 결과를 즉시 반영하기 위해 현재 페이지로 리다이렉팅
	 */
	@PostMapping(value = "updateFunctionalFood.do")
	public String updateFunctionalFood(FunctionalFoodVO vo) {
		System.out.println("controller : updateFunctionalFood.do");
		managerService.updateFunctionalFood(vo);
		logger.log("Manager page: " + "updateFunctionalFood.do");
		return "redirect:manager_nutrient.html";
	}

	/*********************************************************
	 * 함수명 : deleteFunctionalFood
	 * 역할 : 삭제하고자 하는 영양제 정보를 VO에 담아서 가져가, 이를 DB와 조회해 일치하는 정보가 있는 경우 이를 DB에서 삭제함.	 * 
	 * @param vo
	 * @return 삭제가 되면 1, 삭제가 되지 않으면 0 이 돌아옴. 삭제 결과를 즉시 반영하기 위해 현재 페이지로 리다이렉팅.
	 */
	@GetMapping("deleteFunctionalFood.do")
	public String deleteFunction(FunctionalFoodVO vo) {
		managerService.deleteFuctionalFood(vo);
		logger.log("Manager page: " + "deleteFunctionalFood.do");
		return "redirect:manager_nutrient.html";
	}

	/*********************************************************
	 * 함수명 : manager_question
	 * 역할 : 현재 DB에 있는 모든 자가진단 문항을 조회해 이를 VO에 담아서 가져옴. 이 때 한번에 모든 문항이 조회되는 경우 사용자의 편의성이 떨어지므로, 최대 출력 갯수를 제한해 편의성을 향상시킴.
	 * @param m
	 * @param pageNo
	 * @return QuestionVO가 담겨있는 List 와 PageVO
	 */
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

	/*********************************************************
	 * 함수명 : manager_question
	 * 역할 : 현재 DB에 있는 모든 자가진단 문항을 조회해 이를 VO에 담아서 가져옴. 이 때 한번에 모든 문항이 조회되는 경우 사용자의 편의성이 떨어지므로, 최대 출력 갯수를 제한해 편의성을 향상시킴.
	 * @param m
	 * @param pageNo
	 * @return QuestionVO가 담겨있는 List 와 PageVO
	 */
	@GetMapping(value = "deleteQuestion.do")
	public String deleteQuestion(QuestionVO vo) {
		managerService.deleteQuetstion(vo);
		System.out.println("딜리트");
		return "redirect:manager_question.html";
	}

	/*********************************************************
	 * 함수명 : UpdateQuestion
	 * 역할 : 관리자가 자가진단 문항을 수정한 경우, 수정시 입력한 정보들을 VO에 담아 가져가, DB를 조회해 일치하는 ID가 있는 경우, 해당 수정내용을 반영함.
	 * @param vo
	 * @return 수정이 성공하면 1이 실패하면 0 이 돌아옴. 수정 결과를 즉시 반영하기 위해 현재 페이지로 리다이렉팅
	 */
	@PostMapping(value = "updateQuestion.do")
	public String updateQuestion(QuestionVO vo) {
		managerService.updateQuestion(vo);
		System.out.println("업데이트");
		return "redirect:manager_question.html";
	}

	/*********************************************************
	 * 함수명 : InsertQuestion
	 * 역할 : 관리자가 자가진단 문항을 새로이 등록하였을 때, 등록한 문항 및 내용 등 정보들을 VO에 담아 가져가, DB에 이를 입력함.
	 * @param vo
	 * @return 입력이 성공하면 1, 실패하면 0이 돌아옴. 입력 결과를 즉시 반영해 보여줄 수 있도록 현재 페이지로 리다이렉팅.
	 */
	@PostMapping(value = "insertQuestion.do")
	public String insertQuestion(QuestionVO vo) {
		managerService.insertQuestion(vo);
		return "redirect:manager_question.html";
	}

	/*********************************************************
	 * 함수명 : getCoordinates
	 * 역할 : 사용자 메인 페이지에서 사용자가 클릭한 위치를 기록해, 이를 log에 저장함. 이후 log 파일을 읽어 사용자가 클릭한 위치 정보를  Mirror Index에 띄움.
	 * @return
	 */
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

	/*********************************************************
	 * 함수 : getCountPage2
	 * 역할 : 각각의 로그파일을 읽어, 이들 내에 있는 정보를 하나의 문자형 변수에 할당함. 이후 할당된 값을 다시 숫자 형으로 변환해 모델에 담아 View Page에 전달함.
	 * @param m (Model)
	 * @return 모델에 각각의 로그파일 읽은 결과를 담아 보냈음 (즉 로그에 기록된 페이지 별 머무른 시간, 페이지 방문 횟수 등)
	 */
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

	/*********************************************************
	 * 함수명 : SendNews
	 * 역할 : 구독 신청을 한 사용자들에게 자동으로 영양 상식 (크롤링 된 뉴스) 을 등록한 이메일로 전송함.	 
	 */
	@Scheduled(cron = "0 0 1 1 * ?", zone = "Asia/Seoul")
	public void sendNews() {
		managerService.sendNews();
		logger.log("Manager page: " + "sendNews");
	}

	/*********************************************************
	 * 함수명 : getReadForGetCountPage
	 * 역할 : 만들어진 로그 파일에서, 파일에 기록되어 있는 이용 횟수 나 방문횟수 등을 읽어들임.
	 * @param filepath
	 * @return 읽어들인 숫자(이 때 숫자가 기록되어 있더라도 형은 문자형임.)
	 */
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

	/*********************************************************
	 * 함수명 : crawlblog
	 * 역할  : blog.py(기사 크롤링)를 다시 읽고 업데이트 된 기사를 다시 View Page에 전달함. 
	 */
	@RequestMapping("crawlblog.do")
	public String crawlblog() {
		System.out.println("111111");

		try {
			System.out.println("pythonbuilder ");
			ProcessBuilder pb = new ProcessBuilder("python", "crawling/blog.py");
			pb.redirectErrorStream(true);
			Process p = pb.start();
			// int exitval = p.waitFor();
			System.out.println("222222");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("3333333");
		}
		return "redirect:manager_blog.html";

	}

	// 공지사항 목록불러오기
	@GetMapping(value="manager_notice.html")
	public void getListnotice(Model m,@RequestParam(value="pageNo", required=false)String pageNo){
		if(pageNo==null) pageNo = "1";
		Integer totalRecord = managerService.countnotice();
		PaginationVO pageVO = new PaginationVO(Integer.parseInt(pageNo), totalRecord, 10, 5);
		List<NoticeVO> list = managerService.getListnotice(pageVO);
		m.addAttribute("pageVO",pageVO);
		m.addAttribute("noticelist", list);

	}
	// 공지사항 등록
	@PostMapping("insertnotice.do")
	public String insertnotice(NoticeVO vo) {
		managerService.insertnotice(vo);
		return "redirect:manager_notice.html";
	}

	@GetMapping("deletenotice.do")
	public String deletenotice(NoticeVO vo) {
		managerService.deletenotice(vo);
		return "redirect:manager_notice.html";
	}

}
