package com.example.repository;

import com.example.model.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
	@Query("SELECT o FROM Order o WHERE o.customer.id = :userId")
	List<Order> findAllUserOrders(@Param("userId") Long userId);

	@Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId")
	List<Order> findOrdersByRestaurantId(@Param("restaurantId") Long restaurantId);

	@Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.food.id = :foodId AND o.orderStatus = :status")
	List<Order> findByFoodAndStatus(@Param("foodId") Long foodId, @Param("status") String status);


}
