package com.websystique.springmvc.dao;

import java.util.List;

import com.websystique.springmvc.model.User;


public interface UserDao {

	User findById(int id);
	
	User findBySSO(String sso);
	
	User findByName(String name);
	
	void save(User user);
	
	void deleteBySSO(String sso);
	
	List<User> findAllUsers();

}

