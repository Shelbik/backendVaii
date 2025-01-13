package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	PasswordResetToken findByToken(String token);
}
