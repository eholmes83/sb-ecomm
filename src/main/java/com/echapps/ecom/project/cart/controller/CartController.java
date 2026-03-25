package com.echapps.ecom.project.cart.controller;

import com.echapps.ecom.project.cart.dto.request.CartDTO;
import com.echapps.ecom.project.cart.model.Cart;
import com.echapps.ecom.project.cart.repository.CartRepository;
import com.echapps.ecom.project.cart.service.CartService;
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
public class CartController {

    private final CartRepository cartRepository;
    private final CartService cartService;
    private final AuthUtil authUtil;

    public CartController(CartService cartService, CartRepository cartRepository, AuthUtil authUtil) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
    }

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    @Operation(summary = "Add product to cart", description = "Add a product to the shopping cart with specified quantity.")
    @Tag(name = "Cart APIs", description = "APIs for managing shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product added to cart successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CartDTO> addProductToCart(@Parameter(description = "Id of product to add to cart") @PathVariable Long productId,
                                                    @Parameter(description = "Quantity of product to add to cart") @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/allCarts")
    @Operation(summary = "Get all carts", description = "Retrieve a list of all shopping carts in the system. This endpoint is typically used for administrative purposes.")
    @Tag(name = "Cart APIs", description = "APIs for managing shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all carts"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<CartDTO>> getAllUserCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);

    }

    @GetMapping("/users/cart")
    @Operation(summary = "Get user's cart", description = "Retrieve the shopping cart of the currently logged-in user.")
    @Tag(name = "Cart APIs", description = "APIs for managing shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user's cart"),
            @ApiResponse(responseCode = "404", description = "Cart not found for the user", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CartDTO> getCartById() {
        String emailId = authUtil.getLoggedInUserEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();
        CartDTO cartDTO = cartService.getCart(emailId, cartId);
        return new ResponseEntity<>(cartDTO, HttpStatus.FOUND);
    }

    @PutMapping("/carts/products/{productId}/quantity/{operation}")
    @Operation(summary = "Update product quantity in cart", description = "Update the quantity of a specific product in the shopping cart. The operation can be 'add' to increase quantity or 'delete' to decrease quantity.")
    @Tag(name = "Cart APIs", description = "APIs for managing shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation specified", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart or product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CartDTO> updateCartItemQuantity(@Parameter(description = "Id of product to update in cart") @PathVariable Long productId,
                                                          @Parameter(description = "Quantity to add in or remove from cart") @PathVariable String operation) {

       CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation.equalsIgnoreCase("delete") ? -1 : 1);
       return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/products/{productId}")
    @Operation(summary = "Delete product from cart", description = "Remove a specific product from the shopping cart by providing the cart ID and product ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product removed from cart successfully"),
            @ApiResponse(responseCode = "404", description = "Cart or product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Tag(name = "Cart APIs", description = "APIs for managing shopping cart")
    public ResponseEntity<String> deleteProductFromCart(@Parameter(description = "Id of cart to remove product from") @PathVariable Long cartId,
                                                        @Parameter(description = "Id of product to remove from cart") @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
