package com.echapps.ecom.project.cart.service;

import com.echapps.ecom.project.cart.dto.request.CartDTO;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
}
