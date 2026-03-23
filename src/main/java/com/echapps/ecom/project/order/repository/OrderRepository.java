package com.echapps.ecom.project.order.repository;

import com.echapps.ecom.project.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> getOrdersByEmail(String emailId);

    List<Order> findOrdersByEmail(String emailId);
}
