package com.example.service;

import com.stripe.exception.StripeException;
import com.example.model.Order;
import com.example.model.Payment;
import com.example.response.PaymentResponse;

public interface PaymentService {
	
	public PaymentResponse generatePaymentLink(Order order) throws StripeException;
	void savePayment(Payment payment);


}
