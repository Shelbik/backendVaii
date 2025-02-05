package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.OrderItem;
import com.example.repository.OrderItemRepository;
@Service
public class OrderItemServiceImplementation implements OrderItemService {
	@Autowired
	 private OrderItemRepository orderItemRepository;

	    @Override
	    public OrderItem createOrderIem(OrderItem orderItem) {
	    	OrderItem newOrderItem=new OrderItem();
	    	newOrderItem.setQuantity(orderItem.getQuantity());
	        return orderItemRepository.save(newOrderItem);
	    }



}
