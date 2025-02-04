	package com.example.model;

	import com.fasterxml.jackson.annotation.JsonIgnore;
	import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
	import com.example.domain.USER_ROLE;
	import com.example.dto.RestaurantDto;
	import jakarta.persistence.*;
	import lombok.Data;
	import lombok.ToString;

	import java.util.ArrayList;
	import java.util.List;

	@Entity
	@Data

	public class User {

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private Long id;

		private String fullName;
		private String email;
		private String password;

		private USER_ROLE role;

		@JsonIgnore
		@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,orphanRemoval = true)
		private List<Order> orders;

		@ElementCollection
		private List<RestaurantDto> favorites = new ArrayList<>();

		@JsonIgnoreProperties("customer")
		@OneToOne(mappedBy = "customer")
		private Cart cart;

		@JsonIgnoreProperties("users")
		@ManyToMany(cascade = CascadeType.ALL)
		@JoinTable(
				name = "user_addresses", // Имя промежуточной таблицы
				joinColumns = @JoinColumn(name = "user_id"), // Колонка с внешним ключом для user
				inverseJoinColumns = @JoinColumn(name = "address_id") // Колонка с внешним ключом для address
		)
		private List<Address> addresses = new ArrayList<>();

		private String status;
	}
