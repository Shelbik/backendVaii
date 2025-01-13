package com.example.repository;

import com.example.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUsers_Id(Long id);  // Исправленный метод
}




