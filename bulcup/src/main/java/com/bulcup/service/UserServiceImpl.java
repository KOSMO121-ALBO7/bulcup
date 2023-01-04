package com.bulcup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bulcup.dao.UserDao;
import com.bulcup.domain.ContactVO;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userdao;

	public int insertContact(ContactVO vo) {
		return userdao.insertContact(vo);
	}
	
}
