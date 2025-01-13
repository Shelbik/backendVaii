package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
