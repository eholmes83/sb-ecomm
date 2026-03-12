package com.echapps.ecom.project.cart.model;


import com.echapps.ecom.project.product.model.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "cart_items")
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonBackReference("cart-cartItems")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;
    private Double discount;
    private Double productPrice;
}
