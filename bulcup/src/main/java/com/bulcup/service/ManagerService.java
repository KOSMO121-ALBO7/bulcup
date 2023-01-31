package com.bulcup.service;

import java.util.List;

import com.bulcup.domain.BlogVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.PaginationVO;
import com.bulcup.domain.QuestionVO;
import com.bulcup.domain.NoticeVO;

public interface ManagerService {

	public int insertManager(ManagerVO vo);
	public ManagerVO selectManagerByIdAndPw(ManagerVO vo);
	public int updateManager(ManagerVO vo);
	public int deleteManagerById(ManagerVO vo);
	public int updateContact(ContactVO contactVO);
	public int deleteContact(ContactVO vo);
	public List<ManagerVO> getManagerList();
	public List<ManagerVO> getManagerListPg(PaginationVO pageVO);
	public List<ContactVO> getContactList(PaginationVO pageVO);
	public List<ContactVO> getFinishedContactList(PaginationVO pageVO);
	public int insertBlog(BlogVO vo);
	public int updateBlog(BlogVO vo);
	public int deleteBlog(BlogVO vo);
	public List<BlogVO> getBlogList();
	public void sendNews();
	public List<FunctionalFoodVO> getFunctionalFoodListPg(PaginationVO pageVO); 
	public int updateFunctionalFood(FunctionalFoodVO vo);
	// 건강기능식품 정보 삭제
	public int deleteFuctionalFood(FunctionalFoodVO vo);
	public int getPageSize(String pageno);
	public int deleteQuetstion(QuestionVO vo);
	public int updateQuestion(QuestionVO vo);
	public int insertQuestion(QuestionVO vo);
	public List<QuestionVO> getQuestionListPg(PaginationVO pageVO);
	public int questionCount();
	public int foodCount();
	public int managerCount();
	public int waitCount();
	public int completeCount();
	// 공지사항 리스트 출력
	public List<NoticeVO> getListnotice(PaginationVO pageVO);
	// 공지사항 리스트 총 갯수
	public int countnotice();
	public void insertnotice(NoticeVO vo);
	public void deletenotice(NoticeVO vo);

}
