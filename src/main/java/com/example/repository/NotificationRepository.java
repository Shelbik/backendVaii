package com.example.repository;

import java.util.List;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByCustomerId(Long userId);
	List<Notification> findByRestaurantId(Long restaurantId);
	void deleteByCustomer(User customer);

	void deleteByCustomer_Id(Long customerId);






}
