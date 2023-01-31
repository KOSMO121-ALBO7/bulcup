package com.bulcup.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.PaginationVO;
import com.bulcup.domain.QuestionVO;
import com.bulcup.domain.SubscribeVO;
import com.bulcup.domain.NoticeVO;

@Mapper
public interface ManagerDao {

	public int insertManager(ManagerVO vo);
	public ManagerVO selectManagerByIdAndPw(ManagerVO vo);
	public int updateManager(ManagerVO vo);
	public int deleteManagerById(ManagerVO vo);
	public int updateContact(ContactVO vo);
	public int deleteContact(ContactVO vo);
	public List<ManagerVO> getManagerList();
	public List<ManagerVO> getManagerListPg(PaginationVO pageVO);
	public List<ContactVO> getContactList(PaginationVO pageVO);
	public List<ContactVO> getFinishedContactList(PaginationVO pageVO);
	public List<SubscribeVO> getSubscribeList();
	// 모든 건강기능식품 가져오기
	public List<FunctionalFoodVO> getFunctionalFoodListPg(PaginationVO pageVO);
	// 건강기능식품 정보 수정
	public int updateFunctionalFood(FunctionalFoodVO vo);
	// 건강기능식품 정보 삭제
	public int deleteFuctionalFood(FunctionalFoodVO vo);
	public int getPageSize(String pageno);
	public List<QuestionVO> getQuestionListPg(PaginationVO pageVO);
	public int deleteQuestion(QuestionVO vo);
	public int updateQuestion(QuestionVO vo);
	public int insertQuestion(QuestionVO vo);
	public int questionCount();
	public int foodCount();
	public int managerCount();
	public int waitCount();
	public int completeCount();
	public List<NoticeVO> getListnotice(PaginationVO pageVO);
	// 공지사항 리스트 총 갯수
	public int countnotice();
	// 공지사항 입력
	public void insertnotice(NoticeVO vo);
	public void deletenotice(NoticeVO vo);

}
