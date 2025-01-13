package com.example.controller;

import com.stripe.exception.StripeException;
import com.example.Exception.OrderException;
import com.example.model.Order;
import com.example.response.PaymentResponse;
import com.example.service.OrderServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.PaymentService;

@RestController
@RequestMapping("/api")
public class PaymentController {
	
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private OrderServiceImplementation orderServiceImplementation;
	
	@PostMapping("/{orderId}/payment")
	public ResponseEntity<PaymentResponse> generatePaymentLink(@PathVariable Long orderId)
			throws StripeException, OrderException {
		Order order = orderServiceImplementation.findOrderById(orderId);
		PaymentResponse res = paymentService.generatePaymentLink(orderServiceImplementation.findOrderById(orderId));

		return new ResponseEntity<PaymentResponse>(res, HttpStatus.ACCEPTED);
	}

}
