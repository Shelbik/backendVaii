package com.example.model;


import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	@JsonIgnore
	private User customer;

	@JsonIgnore
	@ManyToOne
	private Restaurant restaurant;

	private Long totalAmount;

	private String orderStatus;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@ManyToOne
	private Address deliveryAddress;

	@JsonManagedReference

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items;


	private int totalItem;

	private int totalPrice;

	@OneToOne
	@JoinColumn(name = "payment_id")
	private Payment payment;


}
