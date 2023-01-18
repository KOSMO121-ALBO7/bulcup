package com.bulcup.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bulcup.EmailSender;
import com.bulcup.paging;
import com.bulcup.dao.ManagerDao;
import com.bulcup.domain.BlogVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.PaginationVO;
import com.bulcup.domain.QuestionVO;
import com.bulcup.domain.SubscribeVO;
import com.bulcup.domain.noticeVO;

@Service
public class ManagerServiceImpl implements ManagerService {

	@Autowired
	private ManagerDao managerdao;

	@Autowired
	private EmailSender email;

	public int insertManager(ManagerVO vo) {
		return managerdao.insertManager(vo);
	}

	public ManagerVO selectManagerByIdAndPw(ManagerVO vo) {
		return managerdao.selectManagerByIdAndPw(vo);
	}

	public int updateManager(ManagerVO vo) {
		return managerdao.updateManager(vo);
	}

	public int deleteManagerById(ManagerVO vo) {
		return managerdao.deleteManagerById(vo);
	}

	public int updateContact(ContactVO vo) {
		return managerdao.updateContact(vo);
	}

	public int deleteContact(ContactVO vo) {
		return managerdao.deleteContact(vo);
	}

	public List<ManagerVO> getManagerList() {
		return managerdao.getManagerList();
	}

	public List<ManagerVO> getManagerListPg(PaginationVO pageVO) {
		return managerdao.getManagerListPg(pageVO);
	}

	public List<ContactVO> getContactList(PaginationVO pageVO) {
		return managerdao.getContactList(pageVO);
	}

	public List<ContactVO> getFinishedContactList(PaginationVO pageVO) {
		return managerdao.getFinishedContactList(pageVO);
	}

	public List<BlogVO> getBlogList() {
		List<BlogVO> list = new ArrayList<BlogVO>();
		Scanner sc = null;
		try {
			sc = new Scanner(new FileInputStream("src/main/java/com/bulcup/service/crawling/text.txt"), "UTF-8");
			String line = sc.nextLine();// header 읽기
			int lineEnd = paging.pageNo * paging.pageSize;
			int linePrint = 1;
			int lineStart = (paging.pageNo - 1) * paging.pageSize;
			System.out.println("STARTN END " + lineStart + " " + lineEnd);
			while (sc.hasNext() && linePrint <= lineEnd) {
				line = sc.nextLine();
				if (linePrint > lineStart) {
					System.out.println("lineprint : " + linePrint);
					// System.out.println(line);
					String[] arr = line.split("::");
					BlogVO vo = new BlogVO();
					vo.setUrl(arr[0]);
					vo.setTitle(arr[1]);
					vo.setImg(arr[2]);
					vo.setWriter(arr[3]);
					vo.setTime(arr[4]);
					vo.setContent(arr[5]);
					vo.setClassify(arr[6]);
					list.add(vo);
				}
				linePrint++;
			} // while

		} catch (Exception e) {
			System.out.println("Exception GETBLOGLIST: " + e);
		} finally {
			sc.close();
		}
		return list;
	}// getbloglist

	public int insertBlog(BlogVO vo) {
		File inputFile = new File("src/main/java/com/bulcup/service/crawling/text.txt");
		try {
			String toappend = vo.getUrl() + "::" + vo.getTitle() + "::" + vo.getImg() + "::" + vo.getWriter() + "::"
					+ vo.getTime() + "::" + vo.getContent() + "::" + vo.getClassify() + "\n";
			FileWriter fr = new FileWriter(inputFile, true);
			fr.write(toappend);
			fr.close();
			return 1;
		} catch (Exception e) {
			System.out.println("Exception INSERTBLOG" + e);
		}
		return 0;
	}

	public int updateBlog(BlogVO vo) {
		int a = deleteBlog(vo);
		System.out.println("HERE: " + a);
		int b = insertBlog(vo);
		System.out.println("HERE: " + b);
		if (a + b >= 2)
			return 1;
		return 0;
	}

	public int deleteBlog(BlogVO vo) {
		Scanner sc = null;
		try {
			File inputFile = new File("src/main/java/com/bulcup/service/crawling/text.txt");
			File tempFile = new File("src/main/java/com/bulcup/service/crawling/tempText.txt");
			if (!tempFile.createNewFile())
				tempFile.delete();
			sc = new Scanner(new FileInputStream(inputFile), "UTF-8");
			Writer output = new FileWriter(tempFile);
			String line = sc.nextLine();// header 읽기
			output.write(line + "\n");

			while (sc.hasNext()) {
				line = sc.nextLine();
				String[] arr = line.split("::");
				if (arr[0].equals(vo.getUrl())) {
					System.out.println("deleting" + arr[0]);
					continue; // url이 지우고 싶은 url 과 같으면 지우기
				} else {
					output.write(line + "\n");
				}
			} // while
			output.close();
			sc.close();
			inputFile.delete();
			boolean successful = tempFile.renameTo(inputFile);
			System.out.println("Deleting url:: " + vo.getUrl() + successful);
			return 1;
		} catch (Exception e) {
			System.out.println("Exception GETBLOGLIST: " + e);
		}
		return 0;
	}

	public void sendNews() {
		List<SubscribeVO> subscribeList = managerdao.getSubscribeList();
		List<BlogVO> blogList = getBlogList();
		for (SubscribeVO subscribeVO : subscribeList) {
			HashMap<String, String> values = new HashMap<String, String>();
			values.put("url", blogList.get(0).getUrl());
			values.put("imgurl", blogList.get(0).getImg());
			values.put("title", blogList.get(0).getTitle());
			values.put("content", blogList.get(0).getContent());
			System.out.println(blogList.get(0).getUrl());
			System.out.println(blogList.get(0).getImg());
			email.Send("Bulcup 정기구독", subscribeVO.getEmail(), "/mail-templates/subscribeTemplate", values);
		}
	}

	public int updateFunctionalFood(FunctionalFoodVO vo) {
		return managerdao.updateFunctionalFood(vo);
	}

	public int deleteFuctionalFood(FunctionalFoodVO vo) {
		return managerdao.deleteFuctionalFood(vo);
	}

	public List<FunctionalFoodVO> getFunctionalFoodListPg(PaginationVO pageVO) {
		return managerdao.getFunctionalFoodListPg(pageVO);
	}

	// 페이징 용 크기 획득
	public int getPageSize(String pageno) {
		return managerdao.getPageSize(pageno);
	}

	public int deleteQuetstion(QuestionVO vo) {
		return managerdao.deleteQuestion(vo);
	}

	public int updateQuestion(QuestionVO vo) {
		return managerdao.updateQuestion(vo);
	}

	public int insertQuestion(QuestionVO vo) {
		return managerdao.insertQuestion(vo);
	}

	public List<QuestionVO> getQuestionListPg(PaginationVO pageVO) {
		return managerdao.getQuestionListPg(pageVO);
	}

	public int questionCount() {
		return managerdao.questionCount();
	}

	public int managerCount() {
		return managerdao.managerCount();
	}

	public int foodCount() {
		return managerdao.foodCount();
	}

	public int waitCount() {
		return managerdao.waitCount();
	}

	public int completeCount() {
		return managerdao.completeCount();
	}
	// 공지사항 리스트 출력
	public List<noticeVO> getListnotice(PaginationVO pageVO) {
		return managerdao.getListnotice(pageVO);
	}
	public int countnotice() {
		return managerdao.countnotice();
	}
	public void insertnotice(noticeVO vo) {
		managerdao.insertnotice(vo);
	}

}
