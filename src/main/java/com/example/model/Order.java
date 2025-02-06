package com.example.model;


import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JsonIgnoreProperties("orders")
	private User customer;

	@JsonIgnoreProperties("orders")
	@ManyToOne
	private Restaurant restaurant;

	private Long totalAmount;

	private String orderStatus;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@ManyToOne
	@JsonIgnoreProperties("orders")
	private Address deliveryAddress;

	@JsonIgnoreProperties("order")
	@JoinColumn(name = "order_id")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items;


	private int totalItem;

	private int totalPrice;

	@JsonIgnoreProperties("order")
	@OneToOne
	@JoinColumn(name = "payment_id")
	private Payment payment;


}
