package com.bulcup.service;

import java.util.List;

import com.bulcup.domain.BlogVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.SubscribeVO;

public interface ManagerService {

	public int insertManager(ManagerVO vo);
	public ManagerVO selectManagerByIdAndPw(ManagerVO vo);
	public int updateManager(ManagerVO vo);
	public int deleteManagerById(ManagerVO vo);
	public int updateContact(ContactVO contactVO);
	public int deleteContact(ContactVO vo);
	public List<ManagerVO> getManagerList();
	public List<ContactVO> getContactList();
	public List<ContactVO> getFinishedContactList();
	public int insertBlog(BlogVO vo);
	public int updateBlog(BlogVO vo);
	public int deleteBlog(BlogVO vo);
	public List<BlogVO> getBlogList();
	public void sendNews();
	
}
