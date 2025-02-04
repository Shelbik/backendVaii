package com.example.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties("items")
    @ManyToOne
    private Cart cart;

    @JsonIgnoreProperties({"cart", "restaurant"})
    @ManyToOne
    private Food food;
    
    private int quantity;
    
    @ElementCollection
    private List<String> ingredients;
    
    private Long totalPrice;


}

