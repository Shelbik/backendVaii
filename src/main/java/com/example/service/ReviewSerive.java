package com.example.service;

import java.util.List;

import com.example.Exception.ReviewException;
import com.example.model.Review;
import com.example.model.User;
import com.example.request.ReviewRequest;

public interface ReviewSerive {
	
    public Review submitReview(ReviewRequest review,User user);
    public void deleteReview(Long reviewId) throws ReviewException;
    public double calculateAverageRating(List<Review> reviews);
}
