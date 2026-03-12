package com.echapps.ecom.project.cart.service;

import com.echapps.ecom.project.cart.dto.request.CartDTO;
import com.echapps.ecom.project.cart.model.Cart;
import com.echapps.ecom.project.cart.model.CartItem;
import com.echapps.ecom.project.cart.repository.CartItemRepository;
import com.echapps.ecom.project.cart.repository.CartRepository;
import com.echapps.ecom.project.utils.AuthUtil;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final AuthUtil authUtil;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public CartServiceImpl(CartRepository cartRepository, AuthUtil authUtil, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // 1. Find existing cart for the user (or create a new one if it doesn't exist)
        Cart cart = createCart();

        // 2. Fetch product details using productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // 3. Perform validations (e.g., check if product exists, check stock availability)
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            throw new APIException(product.getProductName() + " is already in the cart. Please update the quantity instead.");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is out of stock.");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Not enough stock for " + product.getProductName() + ". Available quantity: " + product.getQuantity());
        }

        // 4. Create CartItem
        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // 5. Save CartItem
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cart.getCartItems().add(newCartItem);
        cartRepository.save(cart);

        // 6. Return updated cart
        CartDTO cartDTO = mapper.convertValue(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductRequest> productRequestStream = cartItems.stream().map(item -> {
            ProductRequest request = mapper.convertValue(item.getProduct(), ProductRequest.class);
            request.setQuantity(item.getQuantity());
            return request;
        });

        cartDTO.setProducts(productRequestStream.toList());
        return cartDTO;

    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.getLoggedInUserEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.getLoggedInUser());
        return cartRepository.save(cart);
    }
}
