package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.User;
import com.example.service.UserService;

@RestController
@RequestMapping("/super-admin")
public class SupperAdminController {

	@Autowired
	private UserService userService;

	@GetMapping("/customers")
	public ResponseEntity<List<User>> getAllCustomers() {

		List<User> users = userService.findAllUsers();

		return new ResponseEntity<>(users, HttpStatus.ACCEPTED);

	}

	// Удаление пользователя по ID
	@DeleteMapping("/customers/{id}")
	public ResponseEntity<String> deleteUserProfileById(@PathVariable Long id) {
		try {
			userService.deleteUserById(id);
			return new ResponseEntity<>("User deleted successfully", HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>("User not found or an error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
