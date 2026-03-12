package com.echapps.ecom.project.cart.model;

import com.echapps.ecom.project.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "cart", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, orphanRemoval = true)
    @JsonManagedReference("cart-cartItems")
    private List<CartItem> cartItems = new ArrayList<>();

    private Double totalPrice = 0.0;
}
