package com.example.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id") // Связь с таблицей заказов
    private Order order;          // Объект класса Order, а не Long

    private String paymentMethod;
    private String paymentStatus;
    private double totalAmount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
