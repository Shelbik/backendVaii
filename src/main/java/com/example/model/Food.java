package com.example.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private Long price;

    @JsonIgnoreProperties("foods")
    @ManyToOne
    private Category foodCategory;


    @ElementCollection
    @Column(length = 1000)
    private List<String> images;

    private boolean available;

    @JsonIgnoreProperties("foods")
    @ManyToOne
    private Restaurant restaurant;

    private boolean isVegetarian;
    private boolean isSeasonal;
    @JsonIgnoreProperties("foods")
    @ManyToMany
    private List<IngredientsItem> ingredients=new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;



}
