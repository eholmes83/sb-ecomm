package com.echapps.ecom.project.cart.controller;

import com.echapps.ecom.project.cart.dto.request.CartDTO;
import com.echapps.ecom.project.cart.model.Cart;
import com.echapps.ecom.project.cart.repository.CartRepository;
import com.echapps.ecom.project.cart.service.CartService;
import com.echapps.ecom.project.utils.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final CartService cartService;
    private final AuthUtil authUtil;

    public CartController(CartService cartService, CartRepository cartRepository, AuthUtil authUtil) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
    }

    @PostMapping("/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/allUserCarts")
    public ResponseEntity<List<CartDTO>> getAllUserCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);

    }

    @GetMapping("/users/cart")
    public ResponseEntity<CartDTO> getCartById() {
        String emailId = authUtil.getLoggedInUserEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();
        CartDTO cartDTO = cartService.getCart(emailId, cartId);
        return new ResponseEntity<>(cartDTO, HttpStatus.FOUND);
    }

    @PutMapping("/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartItemQuantity(@PathVariable Long productId, @PathVariable String operation) {

       CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation.equalsIgnoreCase("delete") ? -1 : 1);
       return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("{cartId}/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
