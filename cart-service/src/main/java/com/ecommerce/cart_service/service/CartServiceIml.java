package com.ecommerce.cart_service.service;

import com.ecommerce.cart_service.client.ProductClient;
import com.ecommerce.cart_service.dto.AddToCartRequest;
import com.ecommerce.cart_service.dto.CartDTO;
import com.ecommerce.cart_service.dto.CartItemDTO;
import com.ecommerce.cart_service.dto.ProductVariantDTO;
import com.ecommerce.cart_service.exceptions.BadRequestException;
import com.ecommerce.cart_service.exceptions.ResourceNotFoundException;
import com.ecommerce.cart_service.models.Cart;
import com.ecommerce.cart_service.models.CartItem;
import com.ecommerce.cart_service.reponsitory.CartItemRepository;
import com.ecommerce.cart_service.reponsitory.CartRepository;
import com.ecommerce.cart_service.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
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
            item.setDiscountId(variant.getAppliedProductDiscounts().getFirst().getDiscountId());
            item.setSubtotal(currentPrice.multiply(BigDecimal.valueOf(req.getQuantity())));
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepo.save(cart);

        return buildCartDTO(cart);
    }

    @Override
    public CartDTO incrementItem(Long userId, Long itemId) {
        Cart cart = getActiveCart(userId);
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phầm"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Lỗi xác thực, đây không phải giỏ hàng của bạn");
        }

        // LẤY TỒN KHO + GIÁ MỚI NHẤT
        ProductVariantDTO variant = productClient.getVariant(item.getVariantId());
        int newQuantity = item.getQuantity() + 1;

        if (newQuantity > variant.getStockQuantity()) {
            throw new BadRequestException(
                    "Chỉ còn " + variant.getStockQuantity() + " sản phẩm trong kho!"
            );
        }

        item.setQuantity(newQuantity);
        item.setUnitPrice(variant.getFinalPrice());
        item.setSubtotal(variant.getFinalPrice().multiply(BigDecimal.valueOf(newQuantity)));

        cartRepo.save(cart);
        return buildCartDTO(cart);
    }

    @Override
    public CartDTO decrementItem(Long userId, Long itemId) {
        Cart cart = getActiveCart(userId);
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phầm"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Lỗi xác thực, đây không phải giỏ hàng của bạn");
        }

        int currentQuantity = item.getQuantity();

        if (currentQuantity <= 1) {
            // Giảm về 0 → XÓA LUÔN
            cart.getItems().removeIf(i -> i.getId().equals(itemId));
            cartRepo.save(cart);
            return buildCartDTO(cart);
        }

        // Giảm 1
        ProductVariantDTO variant = productClient.getVariant(item.getVariantId());
        int newQuantity = currentQuantity - 1;

        item.setQuantity(newQuantity);
        item.setUnitPrice(variant.getFinalPrice());
        item.setSubtotal(variant.getFinalPrice().multiply(BigDecimal.valueOf(newQuantity)));

        cartRepo.save(cart);
        return buildCartDTO(cart);
    }


    @Override
    public CartDTO updateItem(Long userId, Long itemId, int quantity) {
        Cart cart = getActiveCart(userId);
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Bạn cần đăng nhập lại");
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

        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }

        cartRepo.save(cart); // orphanRemoval = true → tự xóa DB

        return buildCartDTO(cart);
    }



    @Override
    @Transactional
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
                .orElseThrow(() -> new ResourceNotFoundException("Không có thông tin về giỏ hàng của bạn"));
    }

    private CartDTO buildCartDTO(Cart cart) {
        // 1. LẤY DANH SÁCH VARIANT ID
        List<Long> variantIds = cart.getItems().stream()
                .map(CartItem::getVariantId)
                .toList();

        // 2. GỌI 1 LẦN DUY NHẤT → LẤY TẤT CẢ VARIANT
        Map<Long, ProductVariantDTO> variantMap = productClient.getVariantsBatch(variantIds);

        // 3. TẠO DTO
        List<CartItemDTO> dtos = cart.getItems().stream().map(item -> {
            ProductVariantDTO variant = variantMap.get(item.getVariantId());
            if(variant == null) {
                throw new RuntimeException("Sản phẩm không tồn tại hoặc đã bị xóa: variantId=" + item.getVariantId());
            }

            BigDecimal currentPrice = variant.getFinalPrice();
            BigDecimal subtotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            return new CartItemDTO(
                    item.getId(),
                    item.getProductId(),
                    item.getVariantId(),
                    variant.getColor(),
                    variant.getProductName(),
                    variant.getImageUrl(),
                    item.getQuantity(),
                    currentPrice,
                    subtotal,
                    variant.getAppliedProductDiscounts().getFirst().getDiscountId()
            );
        }).toList();

        BigDecimal total = dtos.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(cart.getId(), dtos, total);
    }
}