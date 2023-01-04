package com.bulcup.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.bulcup.domain.ContactVO;
import com.bulcup.domain.ManagerVO;
import com.bulcup.domain.SubscribeVO;

@Mapper
public interface ManagerDao {
	
	public int insertManager(ManagerVO vo);
	public ManagerVO selectManagerByIdAndPw(ManagerVO vo);
	public int updateManager(ManagerVO vo);
	public int deleteManagerById(ManagerVO vo);
	public int updateContact(ContactVO vo);
	public int deleteContact(ContactVO vo);
	public List<ManagerVO> getManagerList();
	public List<ContactVO> getContactList();
	public List<ContactVO> getFinishedContactList();
	public List<SubscribeVO> getSubscribeList();
	
}
