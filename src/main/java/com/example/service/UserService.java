package com.example.service;

import java.util.List;

import com.example.Exception.UserException;
import com.example.model.User;
import com.example.request.UpdateProfileRequest;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;
	
	public User findUserByEmail(String email) throws UserException;

	public List<User> findAllUsers();

	public List<User> getPenddingRestaurantOwner();

	void updatePassword(User user, String newPassword);

	void sendPasswordResetEmail(User user);

	User findUserById(long id) throws Exception;

	User updateUserProfile(String jwt, UpdateProfileRequest updateRequest) throws Exception;


	@Transactional
	void deleteUserByJwtToken(String jwt) throws Exception;

	void deleteUserById(Long id) throws Exception;


}
