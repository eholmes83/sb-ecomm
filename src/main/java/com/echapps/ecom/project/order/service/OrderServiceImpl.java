package com.echapps.ecom.project.order.service;

import com.echapps.ecom.project.cart.model.Cart;
import com.echapps.ecom.project.cart.model.CartItem;
import com.echapps.ecom.project.cart.repository.CartRepository;
import com.echapps.ecom.project.cart.service.CartService;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.order.dto.request.OrderDTO;
import com.echapps.ecom.project.order.dto.request.OrderItemDTO;
import com.echapps.ecom.project.order.model.Order;
import com.echapps.ecom.project.order.model.OrderItem;
import com.echapps.ecom.project.order.model.OrderStatus;
import com.echapps.ecom.project.order.repository.OrderItemRepository;
import com.echapps.ecom.project.order.repository.OrderRepository;
import com.echapps.ecom.project.payment.model.Payment;
import com.echapps.ecom.project.payment.repository.PaymentRepository;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import com.echapps.ecom.project.user.model.Address;
import com.echapps.ecom.project.user.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ObjectMapper mapper;

    public OrderServiceImpl(CartRepository cartRepository, AddressRepository addressRepository, PaymentRepository paymentRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, CartService cartService, ObjectMapper mapper) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId,
                               Long addressId,
                               String paymentMethod,
                               String pgName,
                               String pgPaymentId,
                               String pgStatus,
                               String pgResponseMessage) {

        // 1. Get User Cart
        Cart userCart = cartRepository.findCartByEmail(emailId);
        if (userCart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // 2. Create order with payment info
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(userCart.getTotalPrice());
        order.setOrderStatus(OrderStatus.ACCEPTED.toString());
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        // 3. Get items from card into order items
        List<CartItem> cartItems = userCart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        // 4. Update product stock
        userCart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // 5. Clear user cart
            cartService.deleteProductFromCart(userCart.getCartId(), item.getProduct().getProductId());
        });


        // 6. Send order summary
        OrderDTO orderDTO = mapper.convertValue(savedOrder, OrderDTO.class);
        orderItems.forEach(orderItem -> {
            orderDTO.getOrderItems().add(mapper.convertValue(orderItem, OrderItemDTO.class));
        });

        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
