package com.echapps.ecom.project.cart.service;

import com.echapps.ecom.project.cart.dto.request.CartDTO;
import com.echapps.ecom.project.cart.model.Cart;
import com.echapps.ecom.project.cart.model.CartItem;
import com.echapps.ecom.project.cart.repository.CartItemRepository;
import com.echapps.ecom.project.cart.repository.CartRepository;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import com.echapps.ecom.project.utils.AuthUtil;
import jakarta.transaction.Transactional;
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

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No carts found.");
        }

        return carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = mapper.convertValue(cart, CartDTO.class);
                    List<ProductRequest> products = cart.getCartItems().stream()
                            .map(product -> mapper.convertValue(product.getProduct(), ProductRequest.class))
                            .toList();
                    cartDTO.setProducts(products);
                    return cartDTO;
                }).toList();
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = mapper.convertValue(cart, CartDTO.class);
        cart.getCartItems().forEach(ci -> ci.getProduct().setQuantity(ci.getQuantity()));
        List<ProductRequest> products = cart.getCartItems().stream()
                .map(product -> mapper.convertValue(product.getProduct(), ProductRequest.class))
                .toList();

        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String userEmail = authUtil.getLoggedInUserEmail();
        Cart userCart = cartRepository.findCartByEmail(userEmail);
        Long cartId = userCart.getCartId();

        Cart cartToUpdate = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is out of stock.");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Not enough stock for " + product.getProductName() + ". Available quantity: " + product.getQuantity());
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " is not in the cart. Please add it to the cart first.");
        }

        // Calculate new quantity, validate it, and delete the cart item if the new quantity is zero or negative
        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new APIException("Quantity cannot be negative. Current quantity: " + cartItem.getQuantity());
        }

        if (newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cartToUpdate.setTotalPrice(cartToUpdate.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cartToUpdate);
        }

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = mapper.convertValue(cartToUpdate, CartDTO.class);

        List<CartItem> cartItems = cartToUpdate.getCartItems();

        Stream<ProductRequest> productStream = cartItems.stream().map(ci -> {
            ProductRequest request = mapper.convertValue(ci.getProduct(), ProductRequest.class);
            request.setQuantity(ci.getQuantity());
            return request;
        });

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cartToDeleteProduct = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productIdf", productId);
        }

        cartToDeleteProduct.setTotalPrice(cartToDeleteProduct.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " has been removed from the cart.";
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
