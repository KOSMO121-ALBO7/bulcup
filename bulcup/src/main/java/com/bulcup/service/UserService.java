package com.bulcup.service;

import java.util.HashMap;
import java.util.List;

import com.bulcup.domain.BlogVO;
import com.bulcup.domain.CategoryVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;

public interface UserService {

	int insertContact(ContactVO contactVO);
	public List<BlogVO> getBlogList(int pageNo, int pageSize);
	public List<FunctionalFoodVO> getFunctionalFoodListPg(HashMap map);
	public Integer foodCount(Integer category);
	public Integer foodCount(HashMap searchMap);
	public List<CategoryVO> category();
}
