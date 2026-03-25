package com.echapps.ecom.project.order.controller;

import com.echapps.ecom.project.order.dto.request.OrderDTO;
import com.echapps.ecom.project.order.dto.request.OrderRequestDTO;
import com.echapps.ecom.project.order.service.OrderService;
import com.echapps.ecom.project.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;
    private final AuthUtil authUtil;

    public OrderController(OrderService orderService, AuthUtil authUtil) {
        this.orderService = orderService;
        this.authUtil = authUtil;
    }

    @PostMapping("/orders/users/payments/{paymentMethod}")
    @Tag(name = "Order APIs", description = "APIs for managing orders")
    @Operation(summary = "Create a new order", description = "Create a new order for the currently logged-in user by providing the order details in the request body and specifying the payment method in the path variable.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<OrderDTO> createOrder(@Parameter(description = "Payment method type") @PathVariable String paymentMethod,
                                                @RequestBody OrderRequestDTO orderRequestDTO) {
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
    @Tag(name = "Order APIs", description = "APIs for managing orders")
    @Operation(summary = "Get user's orders", description = "Retrieve a list of all orders placed by the currently logged-in user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user's orders"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        String emailId = authUtil.getLoggedInUserEmail();
        List<OrderDTO> orderDTOS = orderService.getOrdersByUser(emailId);
        return new ResponseEntity<>(orderDTOS, HttpStatus.OK);
    }

    @GetMapping("/orders")
    @Tag(name = "Order APIs", description = "APIs for managing orders")
    @Operation(summary = "Get all orders", description = "Retrieve a list of all orders in the system. This endpoint is typically used for administrative purposes to view all user orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all orders"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orderDTOS = orderService.getAllOrders();
        return new ResponseEntity<>(orderDTOS, HttpStatus.OK);
    }
}
