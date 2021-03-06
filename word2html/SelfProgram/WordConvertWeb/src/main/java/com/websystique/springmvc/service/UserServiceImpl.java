package com.websystique.springmvc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.websystique.springmvc.dao.UserDao;
import com.websystique.springmvc.model.User;


@Service("userService")
@Transactional
public class UserServiceImpl implements UserService{

	@Autowired
	private UserDao dao;

	public User findById(int id) {
		return dao.findById(id);
	}

	public User findBySSO(String sso) {
		User user = dao.findBySSO(sso);
		return user;
	}
	
	public User findByName(String name) {
		User user = dao.findByName(name);
		return user;
	}

	public void saveUser(User user) {
		dao.save(user);
	}

	/*
	 * Since the method is running with Transaction, No need to call hibernate update explicitly.
	 * Just fetch the entity from db and update it with proper values within transaction.
	 * It will be updated in db once transaction ends. 
	 */
	public void updateUser(User user) {
		User entity = dao.findById(user.getId());
		if(entity!=null){
//			entity.setSsoId(user.getSsoId());
			entity.setName(user.getName());
			entity.setPassword(user.getPassword());
			entity.setPasswordConfirm(user.getPasswordConfirm());
			entity.setEmail(user.getEmail());
			entity.setIsadmin(user.getIsadmin());
			entity.setUserDocuments(user.getUserDocuments());
		}
	}

	
	public void deleteUserBySSO(String sso) {
		dao.deleteBySSO(sso);
	}

	public List<User> findAllUsers() {
		return dao.findAllUsers();
	}

	public boolean isUserSSOUnique(Integer id, String sso) {
		User user = findBySSO(sso);
		return ( user == null || ((id != null) && (user.getId() == id)));
	}
	
	public boolean isUserNameUnique(String name) {
		User user = findByName(name);
		return ( user == null );
	}

	public boolean isUserAdmin(String name) {
		// TODO Auto-generated method stub
		User user = findByName(name);
		if(user.getIsadmin())
			return true;
		else
			return false;
	}
	
}
