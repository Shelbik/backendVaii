package com.example.controller;

import com.example.request.UpdateProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Exception.UserException;
import com.example.model.User;
import com.example.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/profile")
	public ResponseEntity<User> getUserProfileHandler(@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserProfileByJwt(jwt);
		user.setPassword(null);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}

	// Обновление информации профиля пользователя
	@PutMapping("/profile")
	public ResponseEntity<User> updateUserProfile(
			@RequestHeader("Authorization") String jwt,
			@RequestBody UpdateProfileRequest updateRequest) {
		try {
			User updatedUser = userService.updateUserProfile(jwt, updateRequest);
			return new ResponseEntity<>(updatedUser, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Удаление пользователя
	@DeleteMapping("/profile")
	public ResponseEntity<String> deleteUserProfile(@RequestHeader("Authorization") String jwt) {
		try {
			String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
			System.out.println(token);
			userService.deleteUserByJwtToken(token);
			return new ResponseEntity<>("User deleted successfully", HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>("User not found or an error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}



}
