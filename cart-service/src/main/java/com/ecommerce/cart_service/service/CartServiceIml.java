package com.ecommerce.cart_service.service;

import com.ecommerce.cart_service.client.ProductClient;
import com.ecommerce.cart_service.dto.AddToCartRequest;
import com.ecommerce.cart_service.dto.CartDTO;
import com.ecommerce.cart_service.dto.CartItemDTO;
import com.ecommerce.cart_service.dto.ProductVariantDTO;
import com.ecommerce.cart_service.exceptions.ResourceNotFoundException;
import com.ecommerce.cart_service.models.Cart;
import com.ecommerce.cart_service.models.CartItem;
import com.ecommerce.cart_service.reponsitory.CartItemRepository;
import com.ecommerce.cart_service.reponsitory.CartRepository;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceIml implements CartService  {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductClient productClient;

    @Override
    public CartDTO getMyCart(Long userId) {
        Cart cart = cartRepo.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> createNewCart(userId));

        return buildCartDTO(cart);
    }


    @Override
    public CartDTO addItem(Long userId, AddToCartRequest req) {
        Cart cart = cartRepo.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> createNewCart(userId));

        ProductVariantDTO variant = productClient.getVariant(req.getVariantId());
        BigDecimal currentPrice = variant.getFinalPrice();

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getVariantId().equals(req.getVariantId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setUnitPrice(currentPrice);  // ← CẬP NHẬT GIÁ
            existing.setSubtotal(currentPrice.multiply(BigDecimal.valueOf(existing.getQuantity())));
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(variant.getProductId());
            item.setVariantId(variant.getVariantId());
            item.setProductName(variant.getProductName());
            item.setImageUrl(variant.getImageUrl());
            item.setQuantity(req.getQuantity());
            item.setUnitPrice(currentPrice);
            item.setSubtotal(currentPrice.multiply(BigDecimal.valueOf(req.getQuantity())));
            item.setReservedUntil(LocalDateTime.now().plusMinutes(15));
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepo.save(cart);

        return buildCartDTO(cart);
    }

    @Override
    public CartDTO updateItem(Long userId, Long itemId, int quantity) {
        Cart cart = getActiveCart(userId);
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("Not your cart");
        }

        if (quantity <= 0) {
            itemRepo.delete(item);
        } else {
            // LẤY GIÁ MỚI NHẤT TỪ product-service
            ProductVariantDTO variant = productClient.getVariant(item.getVariantId());
            BigDecimal currentPrice = variant.getFinalPrice();

            item.setQuantity(quantity);
            item.setUnitPrice(currentPrice);           // ← CẬP NHẬT GIÁ MỚI
            item.setSubtotal(currentPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        return buildCartDTO(cart);
    }

    @Override
    public CartDTO removeItem(Long userId, Long itemId) {
        Cart cart = getActiveCart(userId);
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("Not your cart");
        }

        itemRepo.delete(item);
        return buildCartDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getActiveCart(userId);
        itemRepo.deleteByCartId(cart.getId());
    }


    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepo.save(cart);
    }

    private Cart getActiveCart(Long userId) {
        return cartRepo.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> dtos = cart.getItems().stream().map(item -> {
            // Gọi product-service lấy giá MỚI NHẤT
            ProductVariantDTO variant = productClient.getVariant(item.getVariantId());
            BigDecimal currentPrice = variant.getFinalPrice();
            BigDecimal subtotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            return new CartItemDTO(
                    item.getId(),
                    item.getProductId(),
                    item.getVariantId(),
                    variant.getProductName(),
                    variant.getImageUrl(),
                    item.getQuantity(),
                    currentPrice,           // ← GIÁ MỚI
                    subtotal
            );
        }).toList();

        BigDecimal total = dtos.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(cart.getId(), dtos, total);
    }
}