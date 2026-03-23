package com.echapps.ecom.project.order.controller;

import com.echapps.ecom.project.order.dto.request.OrderDTO;
import com.echapps.ecom.project.order.dto.request.OrderRequestDTO;
import com.echapps.ecom.project.order.service.OrderService;
import com.echapps.ecom.project.utils.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;
    private final AuthUtil authUtil;

    public OrderController(OrderService orderService, AuthUtil authUtil) {
        this.orderService = orderService;
        this.authUtil = authUtil;
    }

    @PostMapping("/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> createOrder(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.getLoggedInUserEmail();
        OrderDTO orderDTO = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @GetMapping("/users/orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        String emailId = authUtil.getLoggedInUserEmail();
        List<OrderDTO> orderDTOS = orderService.getOrdersByUser(emailId);
        return new ResponseEntity<>(orderDTOS, HttpStatus.OK);
    }

    @GetMapping("/allOrders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orderDTOS = orderService.getAllOrders();
        return new ResponseEntity<>(orderDTOS, HttpStatus.OK);
    }
}
