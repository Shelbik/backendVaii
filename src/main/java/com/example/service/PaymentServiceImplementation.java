package com.example.service;

import com.example.model.Order;
import com.example.model.Payment;
import com.example.repository.PaymentRepository;
import com.example.response.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Data
public class PaymentServiceImplementation implements PaymentService{

	@Autowired
	private PaymentRepository paymentRepository;
	@Value("${stripe.api.key}")
	 private String stripeSecretKey;

	@Override
	public PaymentResponse generatePaymentLink(Order order) throws StripeException {

	  Stripe.apiKey = stripeSecretKey;

	        SessionCreateParams params = SessionCreateParams.builder()
	                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
	                .setMode(SessionCreateParams.Mode.PAYMENT)
	                .setSuccessUrl("http://localhost:3000/payment/success/"+order.getId())
	                .setCancelUrl("http://localhost:3000/payment/fail")
	                .addLineItem(SessionCreateParams.LineItem.builder()
	                        .setQuantity(1L)
	                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
	                                .setCurrency("usd")
	                                .setUnitAmount((long) order.getTotalAmount()*100) // Specify the order amount in cents
	                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
	                                        .setName("martiniuc")
	                                        .build())
	                                .build())
	                        .build())
	                .build();
	        
	        Session session = Session.create(params);
	        
	        System.out.println("session _____ " + session);
	        
	        PaymentResponse res = new PaymentResponse();
	        res.setPayment_url(session.getUrl());
	        
	        return res;
	    
	}

	@Override
	public void savePayment(Payment payment) {
		paymentRepository.save(payment);


	}

}
