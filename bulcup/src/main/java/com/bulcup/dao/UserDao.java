package com.bulcup.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.bulcup.domain.CategoryVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.SubscribeVO;
import com.bulcup.domain.SympthomQuestionVO;

@Mapper
public interface UserDao {

	public int insertContact(ContactVO vo);
	public List<FunctionalFoodVO> getFunctionalFoodListPg(HashMap map);
	public Integer foodCount(Integer category_id);
	public Integer foodCountBySearch(HashMap searchMap);
	public List<CategoryVO> category();
	public int insertSubscriber(SubscribeVO vo);
	public List<SympthomQuestionVO> getSympthomQuestion(String category_id);

}
