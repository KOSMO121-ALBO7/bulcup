package com.bulcup.dao;

import org.apache.ibatis.annotations.Mapper;

import com.bulcup.domain.ContactVO;

@Mapper
public interface UserDao {

	public int insertContact(ContactVO vo);

}
