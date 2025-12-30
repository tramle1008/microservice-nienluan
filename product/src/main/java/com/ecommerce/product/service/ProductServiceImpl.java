package com.ecommerce.product.service;

import com.ecommerce.product.client.ImageSearchClient;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.exceptions.ResourceNotFoundException;
import com.ecommerce.product.models.*;
import com.ecommerce.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CategoryService categoryService;
    @Autowired private DiscountRepository discountRepository;
    @Autowired private ProductDiscountRepository productDiscountRepository; // PRODUCT
    @Autowired private VariantDiscountRepository variantDiscountRepository; // VARIANT
    @Autowired
    private ImageSearchClient imageSearchClient;


    @Autowired private FileService fileService;
    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;
    @Override
    public ProductDTO addProductWithVariants(Long categoryId, ProductCreateDTO createDTO,
                                             MultipartFile mainImage, List<MultipartFile> variantImages) throws IOException {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));


        // BƯỚC 1: Tạo và lưu Product trước
        Product product = new Product();
        product.setProductName(createDTO.getProductName());
        product.setShortDescription(createDTO.getShortDescription());
        product.setLongDescription(createDTO.getLongDescription());
        String mainImagePath = fileService.uploadImage("products", mainImage);
        product.setImagePath(mainImagePath);

        product.setPrice(createDTO.getPrice());
        product.setCategory(category);
        product = productRepository.save(product); // ← Lưu để có ID
        // BƯỚC 2: Dùng biến final để lưu product đã có ID
        final Product savedProduct = product;
        // BƯỚC 3: Tạo variants – dùng savedProduct (final)
        List<ProductVariant> variants = createDTO.getVariants().stream().map(v -> {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(savedProduct); // ← OK: savedProduct là final
            variant.setColor(v.getColor());
            variant.setStockQuantity(v.getStockQuantity());
            variant.setPriceOverride(v.getPriceOverride());
            return variant;
        }).collect(Collectors.toList());
        // BƯỚC 4: Upload ảnh variant
        if (variantImages != null && !variantImages.isEmpty()) {
            for (int i = 0; i < Math.min(variants.size(), variantImages.size()); i++) {
                String variantImagePath = fileService.uploadImage("variants", variantImages.get(i));
                variants.get(i).setImagePath(variantImagePath);
            }
        }
        // BƯỚC 5: Lưu variants
        variantRepository.saveAll(variants);

        // BƯỚC 6: Cập nhật finalPrice
// XÓA 2 DÒNG SAU:
//        savedProduct.setFinalPrice(calculateFinalPrice(savedProduct, null));
//        productRepository.save(savedProduct);
        return mapToDTO(savedProduct);
    }

    @Override
    public List<ProductDTO> searchProductsBySimilarImage(MultipartFile image) throws IOException {

        // 1. Gọi Python service
        ImageSearchResponse response = imageSearchClient.searchSimilarImages(image);

        // 2. Lấy danh sách filename từ kết quả
        List<String> similarImageNames = response.similarImages().stream()
                .map(ImageSearchResponse.SimilarImage::filename)
                .toList();

        // 3. Tìm product theo tên ảnh
        Set<Long> productIds = new HashSet<>();

        for (String imageName : similarImageNames) {
            // Loại bỏ phần đuôi nếu cần (ví dụ: có query param ?size=large)
            String cleanName = imageName.split("\\?")[0];

            // Tìm ở ảnh chính
            productRepository.findByImagePathContaining(cleanName)
                    .forEach(p -> productIds.add(p.getProductId()));

            // Tìm ở variant
            variantRepository.findByImagePathContaining(cleanName)
                    .forEach(v -> productIds.add(v.getProduct().getProductId()));
        }

        // 4. Trả về danh sách sản phẩm (có thể sort theo similarity nếu muốn)
        return productRepository.findAllById(productIds)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Page<ProductDTO> getProductsInTree(Long rootId, Pageable pageable) {

        List<Long> categoryIds = categoryService.getAllChildIds(rootId);
        categoryIds.add(rootId); // ← ĐỪNG QUÊN ROOT

        Page<Product> page =
                productRepository.findByCategory_CategoryIdIn(categoryIds, pageable);

        return page.map(this::mapToDTO);
    }


    @Override
    public List<ProductDTO> getDiscountedProducts() {
        LocalDateTime now = LocalDateTime.now();

        // Bước 1: Lấy tất cả discount đang active + trong thời gian
        List<Discount> activeDiscounts = discountRepository.findAll().stream()
                .filter(d -> d.isActive() && d.isWithinDateRange())
                .toList();

        if (activeDiscounts.isEmpty()) {
            return List.of(); // không có discount nào → trả rỗng
        }

        Set<Long> discountedProductIds = new HashSet<>();

        // Bước 2: Từ discount → lấy productId và variantId → suy ra product
        for (Discount discount : activeDiscounts) {
            // 2.1. Discount áp dụng cho PRODUCT
            List<ProductDiscount> productDiscounts = productDiscountRepository.findByDiscountDiscountId(discount.getDiscountId());
            discountedProductIds.addAll(productDiscounts.stream()
                    .map(pd -> pd.getProduct().getProductId())
                    .toList());

            // 2.2. Discount áp dụng cho VARIANT → lấy product từ variant
            List<VariantDiscount> variantDiscounts = variantDiscountRepository.findByDiscountDiscountId(discount.getDiscountId());
            for (VariantDiscount vd : variantDiscounts) {
                ProductVariant variant = vd.getVariant();
                if (variant != null && variant.getProduct() != null) {
                    discountedProductIds.add(variant.getProduct().getProductId());
                }
            }
        }

        if (discountedProductIds.isEmpty()) {
            return List.of();
        }

        // Bước 3: Lấy tất cả product có ID trong danh sách
        List<Product> products = productRepository.findAllByProductIdIn(discountedProductIds);

        // Bước 4: Tính finalPrice và lọc những cái THẬT SỰ có giảm giá
        return products.stream()
                .filter(product -> {
                    // Tính finalPrice cho sản phẩm (có thể có discount ở product hoặc variant)
                    BigDecimal originalPrice = product.getPrice();
                    BigDecimal finalPrice = calculateFinalPrice(product, null); // null = không chỉ định variant

                    return finalPrice.compareTo(originalPrice) < 0; // finalPrice < price gốc → có giảm
                })
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public Page<ProductDTO> getRandomProductsInTree(Long rootId, Pageable pageable) {
        List<Long> categoryIds = categoryService.getAllChildIds(rootId); // ← bạn đã có
        Page<Product> page = productRepository.findRandomInCategoryIds(categoryIds, pageable);
        return page.map(this::mapToDTO);
    }

    @Override
    public ProductVariantDTO getVariantById(Long id) {
        ProductVariant variant = variantRepository.findByIdWithProduct(id)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại id: " + id));
        return mapToVariantDTO(variant);
    }

    @Override
    public ProductVariantDTO increaseVariantStock(Long variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        variantRepository.save(variant);
        return mapToVariantDTO(variant);
    }

    @Override
    public ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder,
                                          String keyword, Long categoryId) {
        Sort sort = sortOrder.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> page;
        if (keyword != null && !keyword.isEmpty()) {
            if (categoryId != null) {
                // Tìm theo keyword + category
                page = productRepository.findAllWithFilters(keyword, categoryId, pageable);
            } else {
                // Chỉ tìm theo keyword
                page = productRepository.searchByKeyword(keyword, pageable);
            }
        } else {
            // Không có keyword → lọc theo category hoặc lấy tất cả
            if (categoryId != null) {
                page = productRepository.findByCategoryCategoryId(categoryId, pageable);
            } else {
                page = productRepository.findAll(pageable);
            }
        }
        List<ProductDTO> dtos = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new ProductResponse(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return mapToDTO(product);
    }
    @Override
    public ProductResponse getProductsByCategory(
            Long categoryId,
            int page,
            int size,
            String sortBy,
            String sortOrder
    ) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage =
                productRepository.findByCategoryCategoryId(categoryId, pageable);

        List<ProductDTO> dtos = productPage
                .getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return new ProductResponse(
                dtos,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }


    @Override
    public ProductResponse getProductsByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> page = productRepository.searchByKeyword(keyword, pageable);
        List<ProductDTO> dtos = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ProductResponse(
                dtos,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    @Override
    public ProductDTO updateProductWithVariants(
            Long productId,
            ProductUpdateDTO updateDTO,
            MultipartFile mainImage,
            List<MultipartFile> variantImages
    ) throws IOException {

        // Lấy product hiện tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // === CẬP NHẬT THÔNG TIN CƠ BẢN ===
        if (updateDTO.getProductName() != null && !updateDTO.getProductName().isBlank())
            product.setProductName(updateDTO.getProductName());
        if (updateDTO.getShortDescription() != null)
            product.setShortDescription(updateDTO.getShortDescription());
        if (updateDTO.getLongDescription() != null)
            product.setLongDescription(updateDTO.getLongDescription());
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
            product.setFinalPrice(updateDTO.getPrice());
        }

        // === UPLOAD ẢNH CHÍNH NẾU CÓ ===
        if (mainImage != null && !mainImage.isEmpty()) {
            String newImagePath = fileService.uploadImage("products", mainImage);
            product.setImagePath(newImagePath);
        }

        // === CẬP NHẬT BIẾN THỂ ===
        if (updateDTO.getVariants() != null && !updateDTO.getVariants().isEmpty()) {
            int imageIndex = 0;

            for (VariantUpdateDTO vDto : updateDTO.getVariants()) {
                if (vDto.getVariantId() == null) continue;

                ProductVariant variant = variantRepository.findById(vDto.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Variant không tồn tại: " + vDto.getVariantId()));

                if (vDto.getColor() != null && !vDto.getColor().isBlank())
                    variant.setColor(vDto.getColor());
                if (vDto.getStockQuantity() != null)
                    variant.setStockQuantity(vDto.getStockQuantity());
                if (vDto.getPriceOverride() != null) {
                    variant.setPriceOverride(vDto.getPriceOverride());
                } else {
                    variant.setPriceOverride(null);
                }

                // UPLOAD ẢNH CHO VARIANT NẾU CÓ
                if (variantImages != null && imageIndex < variantImages.size()) {
                    MultipartFile imgFile = variantImages.get(imageIndex++);
                    if (imgFile != null && !imgFile.isEmpty()) {
                        String newPath = fileService.uploadImage("variants", imgFile);
                        variant.setImagePath(newPath);
                    }
                }
            }
        }

        // Lưu lại
        productRepository.save(product);
        return mapToDTO(product);
    }

    @Override
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }
        variantRepository.deleteByProductProductId(productId);
        productRepository.deleteById(productId);
    }
    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // XÓA ẢNH CŨ
        fileService.deleteImage(p.getImagePath());

        // UPLOAD MỚI → LẤY PATH
        String newPath = fileService.uploadImage("products", image);
        p.setImagePath(newPath);

        productRepository.save(p);
        return mapToDTO(p);
    }
    @Override
    public ProductVariantDTO updateVariantImage(Long variantId, MultipartFile image) throws IOException {
        ProductVariant v = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm được biến thể"));

        fileService.deleteImage(v.getImagePath());

        String newPath = fileService.uploadImage("variants", image);
        v.setImagePath(newPath);

        variantRepository.save(v);
        return mapToVariantDTO(v);
    }
    @Override
    public ProductVariantDTO reduceVariantStock(Long variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Phiên bản sản phẩm không tồn tại"));
        if (variant.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Không đủ sản phẩm trong kho");
        }
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        variantRepository.save(variant);
        // Cập nhật finalPrice của product
//        Product product = variant.getProduct();
//        product.setFinalPrice(calculateFinalPrice(product, null));
//        productRepository.save(product);
        return mapToVariantDTO(variant);
    }
    // Helper: Tính final price
    @Override
    public BigDecimal calculateFinalPrice(Product product, ProductVariant variant) {
        BigDecimal basePrice = variant != null && variant.getPriceOverride() != null
                ? variant.getPriceOverride()
                : product.getPrice();
        if (basePrice == null) return BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        // 1. Discount của Variant (ưu tiên cao)
        if (variant != null) {
            List<Discount> discounts = discountRepository.findActiveDiscountsForVariant(variant.getVariantId());
            for (Discount d : discounts) {
                discountAmount = discountAmount.max(calculateDiscount(d, basePrice));
            }
        }
        // 2. Discount của Product
        if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product.getProductId());
            for (Discount d : discounts) {
                discountAmount = discountAmount.max(calculateDiscount(d, basePrice));
            }
        }
        return basePrice.subtract(discountAmount);
    }
//    @Override
//    public void updateProductFinalPrice(Long productId) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));
//        product.setFinalPrice(calculateFinalPrice(product, null));
//        productRepository.save(product);
//    }

    private BigDecimal calculateDiscount(Discount d, BigDecimal price) {
        if (d.getType() == DiscountType.PERCENTAGE) {
            BigDecimal reduction = price.multiply(d.getPercentage()).divide(BigDecimal.valueOf(100));
            if (d.getMaxAmount() != null) {
                reduction = reduction.min(d.getMaxAmount());
            }
            return reduction;
        } else {
            return d.getMaxAmount() != null ? d.getMaxAmount().min(price) : BigDecimal.ZERO;
        }
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setPrice(product.getPrice());
        dto.setShortDescription(product.getShortDescription());
        dto.setLongDescription(product.getLongDescription());

        // TÍNH 1 LẦN
        dto.setFinalPrice(calculateFinalPrice(product, null));
        dto.setImageUrl(fileService.getFullImageUrl(product.getImagePath()));

        // Áp dụng discount
        List<AppliedDiscountDTO> applied = productDiscountRepository
                .findByProduct_ProductId(product.getProductId())
                .stream()
                .map(pd -> mapToAppliedDiscountDTO(pd.getDiscount()))
                .toList();
        dto.setAppliedDiscounts(applied);

        // Variant
        dto.setVariants(product.getVariants().stream()
                .map(v -> {
                    ProductVariantDTO vDto = new ProductVariantDTO();
                    vDto.setVariantId(v.getVariantId());
                    vDto.setProductId(v.getProduct().getProductId());
                    vDto.setProductName(v.getProduct().getProductName());
                    vDto.setColor(v.getColor());
                    vDto.setStockQuantity(v.getStockQuantity());
                    vDto.setImageUrl(fileService.getFullImageUrl(v.getImagePath()));
                    vDto.setPriceOverride(v.getPriceOverride());

                    // TÍNH FINAL PRICE CHO VARIANT
                    vDto.setFinalPrice(calculateFinalPrice(v.getProduct(), v));

                    // Discount của variant
                    List<AppliedDiscountDTO> vApplied = variantDiscountRepository
                            .findByVariantVariantId(v.getVariantId())
                            .stream()
                            .map(vd -> mapToAppliedDiscountDTO(vd.getDiscount()))
                            .toList();
                    vDto.setAppliedDiscounts(vApplied);
                    return vDto;
                })
                .toList());

        return dto;
    }

    private ProductVariantDTO mapToVariantDTO(ProductVariant v) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setVariantId(v.getVariantId());
        dto.setProductId(v.getProduct().getProductId());
        dto.setProductName(v.getProduct().getProductName());
        dto.setColor(v.getColor());
        dto.setStockQuantity(v.getStockQuantity());
        dto.setImageUrl(fileService.getFullImageUrl(v.getImagePath()));
        dto.setPriceOverride(v.getPriceOverride());
        dto.setProductId(v.getProduct().getProductId());
        dto.setProductName(v.getProduct().getProductName());
        dto.setFinalPrice(calculateFinalPrice(v.getProduct(), v));

//        Discount Product
        List<AppliedDiscountDTO> productDiscounts = productDiscountRepository
                .findByProduct_ProductId(v.getProduct().getProductId())
                .stream()
                .map(pd -> mapToAppliedDiscountDTO(pd.getDiscount()))
                .toList();

        dto.setAppliedProductDiscounts(productDiscounts);

// Discount Variant
        List<AppliedDiscountDTO> applied = variantDiscountRepository
                .findByVariantVariantId(v.getVariantId())  // Lấy List<VariantDiscount>
                .stream()
                .map(vd -> mapToAppliedDiscountDTO(vd.getDiscount())) // ← DỮ LIỆU CÓ SẴN!
                .collect(Collectors.toList());

        dto.setAppliedDiscounts(applied);
        return dto;
    }

    private AppliedDiscountDTO mapDiscount(Discount d) {
        AppliedDiscountDTO dto = new AppliedDiscountDTO();
        dto.setDiscountId(d.getDiscountId());
        dto.setName(d.getName());
        dto.setPercentage(d.getPercentage());
        dto.setMaxAmount(d.getMaxAmount());
        dto.setStartDate(d.getStartDate());
        dto.setEndDate(d.getEndDate());
        return dto;
    }
    private AppliedDiscountDTO mapToAppliedDiscountDTO(Discount d) {
        return new AppliedDiscountDTO(
                d.getDiscountId(),
                d.getName(),
                d.getPercentage(),
                d.getMaxAmount(),
                d.getStartDate(),
                d.getEndDate()
        );
    }
}