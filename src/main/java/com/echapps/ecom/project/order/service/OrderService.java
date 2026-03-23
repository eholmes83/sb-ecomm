package com.echapps.ecom.project.order.service;

import com.echapps.ecom.project.order.dto.request.OrderDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {

    @Transactional
    OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);

    List<OrderDTO> getOrdersByUser(String emailId);

    List<OrderDTO> getAllOrders();
}
