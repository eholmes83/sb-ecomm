package com.echapps.ecom.project.order.dto.request;


import com.echapps.ecom.project.payment.dto.request.PaymentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @Schema(description = "Unique identifier for the order", example = "1")
    private Long orderId;

    @Schema(description = "Email address of the customer placing the order", example = "john_doe@hotmail.com")
    private String email;

    @Schema(description = "List of items included in the order")
    List<OrderItemDTO> orderItems;

    @Schema(description = "Date when the order was placed", example = "2024-06-01")
    LocalDate orderDate;

    @Schema(description = "Payment details associated with the order")
    PaymentDTO payment;

    @Schema(description = "Total amount for the order", example = "999.99")
    private Double totalAmount;

    @Schema(description = "Current status of the order", example = "PENDING")
    private String orderStatus;

    @Schema(description = "Unique identifier for the shipping address associated with the order", example = "1")
    private Long addressId;
}
