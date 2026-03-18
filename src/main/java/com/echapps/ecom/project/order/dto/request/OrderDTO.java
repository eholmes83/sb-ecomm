package com.echapps.ecom.project.order.dto.request;


import com.echapps.ecom.project.payment.dto.request.PaymentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String email;
    List<OrderItemDTO> orderItems;
    LocalDate orderDate;
    PaymentDTO payment;
    private Double totalAmount;
    private String orderStatus;
    private Long addressId;
}
