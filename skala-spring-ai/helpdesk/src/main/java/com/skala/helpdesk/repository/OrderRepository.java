package com.skala.helpdesk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skala.helpdesk.domain.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

    default Optional<Order> findOwned(String id, String ownerId) {
        return findByIdAndOwnerId(id, ownerId);
    }

    Optional<Order> findByIdAndOwnerId(String id, String ownerId);
}
