	package com.example.model;

	import com.fasterxml.jackson.annotation.JsonBackReference;

	import com.fasterxml.jackson.annotation.JsonIgnore;
	import jakarta.persistence.*;
	import lombok.AllArgsConstructor;
	import lombok.Data;
	import lombok.NoArgsConstructor;

	import java.util.ArrayList;
	import java.util.List;

	@Entity
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public class Address {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		private String fullName;

		private String streetAddress;

		private String city;

		private String state;

		private String postalCode;

		private String country;
		@ManyToMany(cascade = CascadeType.ALL)
		private List<User> users = new ArrayList<>();
	}
