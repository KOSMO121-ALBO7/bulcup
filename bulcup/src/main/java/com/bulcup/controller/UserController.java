package com.bulcup.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bulcup.BulcupLogger;
import com.bulcup.MyLogger;
import com.bulcup.paging;
import com.bulcup.domain.BlogVO;
import com.bulcup.domain.CategoryVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.LifestyleGroupVO;
import com.bulcup.domain.LifestyleQuestionVO;
import com.bulcup.domain.PaginationVO;
import com.bulcup.domain.RawMaterialVO;
import com.bulcup.domain.SelfDiagnosisVO;
import com.bulcup.domain.SubscribeVO;
import com.bulcup.domain.SympthomQuestionVO;
import com.bulcup.service.UserService;

@Controller
@RequestMapping("user")
public class UserController {
	@Autowired
	private UserService userService;
	MyLogger logger = MyLogger.getLogger();
	BulcupLogger logUsers;
	BulcupLogger logFavoritePages;
	BulcupLogger logTimer;
	BulcupLogger logSearch;
	Long prevTime = Long.valueOf(LocalTime.now().format(DateTimeFormatter.ofPattern("mm")));
	String prevPage = null;
	String nextPage = null;

	@GetMapping("/{step}")
	public String viewPage(@PathVariable String step, HttpServletRequest req, HttpServletResponse res) {
		if(prevPage == null) prevPage = step.replace(".html", "");
		logUser(req, res, step.replace(".html", "") + "\n");
		logFavoritePages(step.replace(".html", ""));
		logTime(step.replace(".html", ""));
		logger.log("User page: " + step);
		return "/user/" + step;
	} // viewPage

	@GetMapping(value = "index.html")
	public String index(Model m, HttpServletRequest req, HttpServletResponse res) {
		if(prevPage == null) prevPage = "index";
		logUser(req, res, "index\n");
		logFavoritePages("index");
		logTime("index");
		logger.log("User page: " + "index.html");
		List<BlogVO> list = userService.getBlogList(1, 3);
		m.addAttribute("getBlogList", list);
		return "/user/index.html";
	}

	@RequestMapping("insertContact.do")
	public String insertContact(ContactVO contactVO) {
		logger.log("User page: " + "insertContact.do");
		System.out.println(":INSert Contact");
		System.out.println("EMAU:L " + contactVO.getEmail());

		int result = userService.insertContact(contactVO);
		System.out.println("INSERTING CONTACT: " + result);
		return "redirect:index.html";
	}

	@GetMapping(value = "blog.html")
	public String blog(Model m, @RequestParam(value = "pageNo", required = false) String pageno, 
			HttpServletRequest req, HttpServletResponse res) {
		if(prevPage == null) prevPage = "blog";
		logUser(req, res, "blog\n");
		logFavoritePages("blog");
		logTime("blog");
		logger.log("User page: " + "blog.html");
		if (pageno != null) paging.pageNo = Integer.valueOf(pageno);
		else paging.pageNo = 1;
		m.addAttribute("totalPageNo", paging.totalPageNo);
		m.addAttribute("pageNo", paging.pageNo);
		List<BlogVO> list = userService.getBlogList(paging.pageNo, paging.pageSize);
		m.addAttribute("getBlogList", list);
		return "/user/blog.html";
	}

	// name: logUser
	// param: HttpServletRequest, HttpServletResponse, String = pathURL
	// function: ID라는 쿠키를 만약 쿠키가 존재하지 않는다면 만들고 pathURL 을 각각 만들어진 User+ID.log 에 저장한다
	// countUsers.log은 별(*) 개수가 쿠키 한개에 해당해 사이트 이용자 수를 세는데 써짐
	public void logUser(HttpServletRequest req, HttpServletResponse res, String pageName) {
		Cookie cookie = null;
		boolean cookieExists = false;

		if (req.getCookies() != null) {
			for (Cookie c : req.getCookies()) {
				if (c.getName().equals("ID")) {
					cookieExists = true;
					// cookie 들 중 ID 라는 쿠키가 있다면 그 유저 파일을 만들어 저장한다.
					logUsers = BulcupLogger.getLogger("log/users/user" + c.getValue() + ".log");
					break;
				}
			} // for
		} // cookie 값이 있다면 cookieExists == true
		if (!cookieExists) {
			System.out.println("내가 만든 Cookie ~ ^^");
			// if cookie가 없다면 ID라는 쿠키를 만든다.
			cookie = new Cookie("ID", UUID.randomUUID().toString());
			res.addCookie(cookie);			
			logUsers = BulcupLogger.getLogger("log/countUsers.log", 0); // 새로운 쿠키가 만들어진다면 새로운 사람이 사이트에 들어 온 것
			logUsers.logIncrement("1"); // 사이트 사용자 수를 세기 위해 파일에 사람 한명당 1씩 늘린다
			// 새로 만든 cookie ID에 그 유저 파일을 만들어 저장한다.
			logUsers = BulcupLogger.getLogger("log/users/user" + cookie.getValue() + ".log");
		}
		// 유저파일에 경로를 적어넣는다.
		logUsers.log(pageName);

	} // logUser

	// name: logFavoritePages
	// param: HttpServletRequest, HttpServletResponse, String = pathURL
	// function: pathURL.log 에 URL을 한 번 다녀올때마다 그 숫자 카운트를 1 더해준다. 쿠키값과 상관없는 진짜 페이지 방문 횟수이다.
	public void logFavoritePages(String page) {
		logFavoritePages = BulcupLogger.getLogger("log/pages/page" + page + ".log", 0);
		logFavoritePages.logIncrement("1");
	}// logFavoritePages

	// name: logTime
	// param: HttpServletRequest, HttpServletResponse, String = pathURL
	// function: prevPage.log 에 사용자가 (다른페이지로 이동하지 전까지) 머문 시간을 더해준다. 
	// 문제점: 마지막 페이지의 체류시간을 구하지 못한다, 사용자가 다른 웹사이트에 들렸다 페이지로 다시 돌아와도
	//      구분을 못하고 사이를 떠났던 시간까지 마지막에 있었던 페이지에 더해진다(이것도 5분컷).
	public void logTime(String page) {
		LocalTime Now = LocalTime.now();
		Long formattedNow = Long.valueOf(Now.format(DateTimeFormatter.ofPattern("mm")));
		System.out.println("FORMATTED NOW : " + formattedNow);
		if(formattedNow - prevTime > 5) { //한번 페이지 랜딩을 했을 때 기록할 수 있는 시간을 5분이상 못 넘게 한다.
			Now = LocalTime.now();
			formattedNow = Long.valueOf(Now.format(DateTimeFormatter.ofPattern("mm")));
			prevTime = formattedNow; // 그 이유는 사용자가 페이지에서 빠져나가면 그 이벤트를 잡을 수 있는 자바스크립트가 chrome과 연동해서는 내가 아는 한 존재하지 않는다.
		} //그러므로 마지막으로 본 페이지는 자동적으로 시간이 0 이 된다. 하지만 사용자가 그 페이지를 5분 이상 뚫어져라 보고 있을떄도 0이 된다.
		logTimer = BulcupLogger.getLogger("log/time/time" + prevPage + ".log");
		logTimer.logIncrementTime(formattedNow - prevTime);
		prevTime = formattedNow;
		prevPage = page;
	}// logFavoritePages

	public void logSearch(String condition, String keyword) {
		logSearch = BulcupLogger.getLogger("log/search/"+condition+".log");
		logSearch.log(keyword);
	}

	/**********************************
	 * 
	 */
	@GetMapping(value = "search.html")
	public String searchFood(Model m, HttpServletRequest req, HttpServletResponse res,
			@RequestParam(value = "pageNo", required = false) String pageno,		// 페이지번호 파라미터
			@RequestParam(value = "category", required = false) String cate,		// 카테고리번호 파라미터
			@RequestParam(value = "condition", required = false) String condition,	// 검색타입 파라미터
			@RequestParam(value = "keyword", required = false) String keyword) {	// 키워드 파라미터

		if(prevPage == null) prevPage = "search";

		// 요청한 페이지번호가 있으면 형변환 없으면 1
		Integer pageNo = (pageno == null) ? 1 : Integer.valueOf(pageno);
		Integer category_id = null;
		Integer totalRecord = null;
		HashMap searchMap = new HashMap<>();
		HashMap map = new HashMap();

		if(condition == null) {
			// 카테고리 클릭시
			// 요청 카테고리가 있으면 형변환 없으면 0
			category_id = (cate == null) ? 0 : Integer.valueOf(cate);
			totalRecord = userService.foodCount(category_id);
		} else {
			// 검색시
			String[] keywordArr = keyword.split("[^가-힣A-z0-9]");
			searchMap.put("condition", condition);
			searchMap.put("keywordArr", keywordArr);
			totalRecord = userService.foodCount(searchMap);

			logSearch(condition, keyword + "\n");
		}

		// 페이지네이션VO 생성
		PaginationVO pageVO = new PaginationVO(pageNo, totalRecord, 15, 5);
		// 맵에 페이지네이션VO, 카테고리번호, 검색타입, 검색키워드 넣고 인자로 넘긴다
		map.put("category_id", category_id);
		map.put("pageVO", pageVO);
		map.put("searchMap", searchMap);
		List<FunctionalFoodVO> list = userService.getFunctionalFoodListPg(map);
		List<CategoryVO> categoryList = userService.category();
		// 결과 모델에 담아 리턴
		m.addAttribute("list", list);
		m.addAttribute("category_id", category_id);
		m.addAttribute("condition", condition);
		m.addAttribute("keyword", keyword);
		m.addAttribute("pageVO", pageVO);
		m.addAttribute("categoryList", categoryList);
		logUser(req, res, "search\n");
		logFavoritePages("search");
		logTime("search");
		logger.log("User page: " + "search.html");
		return "/user/search.html";
	}

	@PostMapping(value = "saveCoordinates")
	@ResponseBody
	public String saveCoordinates(String x, String y, String tourl) {
		System.out.println("MEE3E: " + x + "/" + tourl);
		BulcupLogger logxCoord = BulcupLogger.getLogger("log/logCoord/logxcoord.log");
		BulcupLogger logyCoord = BulcupLogger.getLogger("log/logCoord/logycoord.log");
		logxCoord.log(x + "-");
		logyCoord.log(y + "-");
		return "ok";
	}

	@PostMapping(value="insertSubscriber.do")
	@ResponseBody
	public void insertSubscriber (SubscribeVO vo) {
		userService.insertSubscriber(vo);
	};

	/****************************************
	 * 함수명 : detail
	 * 역할 : 사용자가 검색을 통해 원하는 영양제 사진을 선택했을 때, 해당 제품의 상세정보 페이지로 넘어가 정보 출력
	 * @param model
	 * @param id
	 * @return : 상세정보 페이지
	 */
	@RequestMapping(value="Detail.html")
	public String detail(Model m , @RequestParam(value="id", required=false) String id) {
		FunctionalFoodVO result = userService.detail(id);
		m.addAttribute("result", result);
		return "user/Detail.html";
	}

	@RequestMapping(value = "selfDiagnosisCategory.html")
	public String selfDiagnosisCategory(Model m, SelfDiagnosisVO vo, HttpSession session) {
		System.out.println("selfDiagnosisCategory");
		if(vo.getName() != null) session.setAttribute("result", vo);

		Object obj = session.getAttribute("result");
		if(obj == null) return "/user/selfDiagnosisStart.html";

		List<CategoryVO> categoryList = userService.category();
		categoryList.remove(0);
		m.addAttribute("categoryList", categoryList);
		m.addAttribute("title", "관심있는 분야를 선택해주세요. (최대 3개)");
		return "/user/selfDiagnosisCategory.html";
	}


	@RequestMapping(value = "selfDiagnosisSympthom.html")
	public String selfDiagnosisSympthom(Model m, HttpSession session, HttpServletRequest req,
			@RequestParam(value = "page", required = false) String page) {
		System.out.println("selfDiagnosisSympthom");

		Object obj = session.getAttribute("result");
		if(obj == null) return "/user/selfDiagnosisStart.html";

		// 증상문항 첫 시작
		if(page == null) {
			System.out.println("selfDiagnosisSympthomStart");
			String[] categoryIdArr = req.getParameterValues("category");
			ArrayList<String> category = new ArrayList<>();
			List<List<SympthomQuestionVO>> sympthomList = new ArrayList<>();

			// 증상문항 DB에서 검색 후 세션에 저장
			for(int i = 0; i < categoryIdArr.length; i++) {
				List<SympthomQuestionVO> list = userService.getSympthomQuestion(categoryIdArr[i]);
				CategoryVO categoryVO = userService.getCategory(categoryIdArr[i]);
				sympthomList.add(list);
				category.add(categoryVO.getCategory());
			}
			int currIndex = 0;
			int lastIndex = sympthomList.size() - 1;
			session.setAttribute("sqCurrIndex", currIndex);
			session.setAttribute("sqLastIndex", lastIndex);
			session.setAttribute("sympthomList", sympthomList);
			session.setAttribute("category", category);

			m.addAttribute("category", category.get(currIndex));
			m.addAttribute("questionList", sympthomList.get(currIndex));
			m.addAttribute("title", "해당하는 항목을 선택해주세요.");

			// 이전버튼 눌렀을 때
		} else if(page.equals("prev")) {
			System.out.println("selfDiagnosisSympthomPrev");
			int currIndex = (int) session.getAttribute("sqCurrIndex");

			// 첫 증상문항일 때 모델에 관심사항문항 담아 관심사항문항으로 리턴
			if (currIndex == 0) return "redirect:/user/selfDiagnosisCategory.html";

			currIndex--;
			session.setAttribute("sqCurrIndex", currIndex);
			List<List<SympthomQuestionVO>> sympthomList = (List<List<SympthomQuestionVO>>) session.getAttribute("sympthomList");
			ArrayList<String[]> sympthom = (ArrayList<String[]>) session.getAttribute("sympthom");
			int lastIndex = sympthom.size() - 1;
			// 리스트에서 마지막 증상 삭제 후 세션에 저장
			sympthom.remove(lastIndex);
			session.setAttribute("sympthom", sympthom);

			System.out.println(sympthom);

			// 이전 증상문항 모델에 담아 리턴
			List<String> category = (List<String>) session.getAttribute("category");
			m.addAttribute("category", category.get(currIndex));
			m.addAttribute("questionList", sympthomList.get(currIndex));
			m.addAttribute("title", "해당하는 문항을 선택해주세요.");

			// 다음버튼 눌렀을 때
		} else if(page.equals("next")) {
			System.out.println("selfDiagnosisSympthomNext");
			// 선택리스트 세션에 저장
			ArrayList<String[]> sympthom = (ArrayList<String[]>) session.getAttribute("sympthom");
			if (sympthom == null) sympthom = new ArrayList<>();
			String[] sympthomArr = req.getParameterValues("sympthom");
			sympthom.add(sympthomArr);
			session.setAttribute("sympthom", sympthom);

			System.out.println(sympthom);

			// 현재 인덱스 마지막 인덱스 세션에서 가져오기
			int currIndex = (int) session.getAttribute("sqCurrIndex");
			int lastIndex = (int) session.getAttribute("sqLastIndex");

			// 마지막 페이지 비교
			if(currIndex == lastIndex) return "redirect:/user/selfDiagnosisLifestyle.html";

			currIndex++;
			session.setAttribute("sqCurrIndex", currIndex);
			List<List<SympthomQuestionVO>> sympthomList = (List<List<SympthomQuestionVO>>) session.getAttribute("sympthomList");
			List<String> category = (List<String>) session.getAttribute("category");
			m.addAttribute("category", category.get(currIndex));
			m.addAttribute("questionList", sympthomList.get(currIndex));
			m.addAttribute("title", "해당하는 문항을 선택해주세요.");
		}

		return "/user/selfDiagnosisSympthom.html";

	}

	@RequestMapping(value = "selfDiagnosisLifestyle.html")
	public String selfDiagnosisLifestyle(Model m, HttpSession session, HttpServletRequest req,
			@RequestParam(value = "page", required = false) String page) {
		System.out.println("selfDiagnosisLifestyle");

		Object obj = session.getAttribute("result");
		if(obj == null) return "/user/selfDiagnosisStart.html";

		// 생활습관문항 첫 시작
		if(page == null) {
			System.out.println("selfDiagnosisLifestyleStart");

			// 생활습관문항 DB에서 검색후 세션에 저장
			List<List<LifestyleQuestionVO>> lifestyleList = new ArrayList<>();
			List<LifestyleGroupVO> voList = userService.getLifestyleGroupList();
			for (LifestyleGroupVO vo : voList) {
				List<LifestyleQuestionVO> list = userService.getLifestyleQuestionList(vo.getId());
				lifestyleList.add(list);
				System.out.println("getLifestyleQuestionList:"+list);
			}
			int currIndex = 0;
			int lastIndex = lifestyleList.size() - 1;
			session.setAttribute("lqCurrIndex", currIndex);
			session.setAttribute("lqLastIndex", lastIndex);
			session.setAttribute("lifestyleList", lifestyleList);
			m.addAttribute("questionList", lifestyleList.get(currIndex));
			m.addAttribute("title", "해당하는 문항을 선택해주세요.");
			m.addAttribute("group", lifestyleList.get(currIndex).get(0).getLifestyle_group());

			// 이전버튼 눌렀을 때
		} else if(page.equals("prev")) {
			System.out.println("selfDiagnosisLifestylePrev");
			int currIndex = (int) session.getAttribute("lqCurrIndex");

			// 첫 생활습관문항일 때 마지막 증상문항으로 이동
			if (currIndex == 0) {
				ArrayList<String[]> sympthom = (ArrayList<String[]>) session.getAttribute("sympthom");
				List<List<SympthomQuestionVO>> sympthomList = (List<List<SympthomQuestionVO>>) session.getAttribute("sympthomList");
				int lastIndex = (int) session.getAttribute("sqLastIndex");
				sympthom.remove(lastIndex);
				session.setAttribute("sympthom", sympthom);

				System.out.println(sympthom);

				m.addAttribute("questionList", sympthomList.get(lastIndex));
				m.addAttribute("title", "해당하는 문항을 선택해주세요.");
				return "/user/selfDiagnosisSympthom.html";
			}

			currIndex--;
			session.setAttribute("lqCurrIndex", currIndex);
			List<List<LifestyleQuestionVO>> lifestyleList = (List<List<LifestyleQuestionVO>>) session.getAttribute("lifestyleList");
			ArrayList<String[]> lifestyle = (ArrayList<String[]>) session.getAttribute("lifestyle");
			// 생활습관문항 리스트에서 삭제 후 세션에 저장
			int lastIndex = lifestyle.size() - 1;
			lifestyle.remove(lastIndex);
			session.setAttribute("lifestyle", lifestyle);

			System.out.println(lifestyle);

			// 이전 생활습관문항 모델에 담아 리턴
			m.addAttribute("questionList", lifestyleList.get(lastIndex));
			m.addAttribute("title", "해당하는 문항을 선택해주세요.");
			m.addAttribute("group", lifestyleList.get(currIndex).get(0).getLifestyle_group());

			// 다음버튼 눌렀을 때
		} else if(page.equals("next")) {
			System.out.println("selfDiagnosisLifestyleNext");
			// 선택리스트 세션에 저장
			ArrayList<String[]> lifestyle = (ArrayList<String[]>) session.getAttribute("lifestyle");
			if (lifestyle == null) lifestyle = new ArrayList<>();
			String[] lifestyleArr = req.getParameterValues("lifestyle");
			lifestyle.add(lifestyleArr);
			session.setAttribute("lifestyle", lifestyle);

			System.out.println(lifestyle);

			// 현재 인덱스 마지막 인덱스 세션에서 가져오기
			int currIndex = (int) session.getAttribute("lqCurrIndex");
			int lastIndex = (int) session.getAttribute("lqLastIndex");

			// 마지막 페이지일 때
			if(currIndex == lastIndex) {
				////////////////// 결과값 result
				ArrayList<String> category = (ArrayList<String>) session.getAttribute("category");
				ArrayList<String[]> sympthom = (ArrayList<String[]>) session.getAttribute("sympthom");

				List result = new ArrayList<>();

				for (int i = 0; i < sympthom.size(); i++) {
					// 선택한 증상문항 관련 원재료id 검색후 Set에 저장 (중복제거)
					HashMap resultMap = new HashMap<>();
					List materials = new ArrayList<>();
					Set<String> tempSet = new HashSet<>();
					for(String sympthomQuestionId : sympthom.get(i)) {
						List<String> rawMaterialList = userService.getRawMaterialIdList(sympthomQuestionId);
						tempSet.addAll(rawMaterialList);
					}
					// 원재료 id로 원재료 검색 후 데이터 처리 및 리턴리스트에 저장
					for(String id : tempSet) {
						List<RawMaterialVO> rawMaterialList = userService.getRawMaterialList(id);
						List<String> functionalityList = new ArrayList<>();
						for (RawMaterialVO vo : rawMaterialList) {
							String[] tempArr = vo.getFunctionality().split("/");
							for(int j = 0; j < tempArr.length; j++) functionalityList.add(tempArr[j]);
							HashMap material = new HashMap<>();
							material.put("name", vo.getRaw_material_name());
							material.put("functionality", functionalityList);
							materials.add(material);
						}
					}
					resultMap.put("category", category.get(i));
					resultMap.put("materials", materials);
					result.add(resultMap);
				};
				m.addAttribute("result", result);
				return "/user/selfDiagnosisEnd.html";
			}

			currIndex++;
			session.setAttribute("lqCurrIndex", currIndex);
			List<List<LifestyleQuestionVO>> lifestyleList = (List<List<LifestyleQuestionVO>>) session.getAttribute("lifestyleList");
			m.addAttribute("questionList", lifestyleList.get(currIndex));
			m.addAttribute("title", "해당하는 문항을 선택해주세요.");
			m.addAttribute("group", lifestyleList.get(currIndex).get(0).getLifestyle_group());
		}

		return "/user/selfDiagnosisLifestyle.html";

	}

}