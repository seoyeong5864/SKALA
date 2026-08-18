package com.skala.ai.lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skala.ai.lab.domain.Order;
import com.skala.ai.lab.domain.Order.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByIdAndOwnerId(String id, String ownerId);

    List<Order> findTop5ByOwnerIdOrderByOrderedAtDesc(String id, String ownerId);

    @Query("select o from Order o "
         + " where o.ownerId = :ownerId "
         + "   and o.status in :statuses "
         + " order by o.orderedAt desc")
    List<Order> findByOwnerAndStatuses(@Param("ownerId") String ownerId,
                                       @Param("statuses") List<OrderStatus> statuses);
}