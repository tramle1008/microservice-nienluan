package com.ecommerce.product.service;

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
import java.util.List;
import java.util.Objects;
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

    @Autowired private FileService fileService;
    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;
    @Override
    public List<ProductDTO> getDiscountedProducts() {
        // Lấy tất cả sản phẩm
        List<Product> allProducts = productRepository.findAll();

        // Lọc sản phẩm có ít nhất 1 discount active
        List<Product> discountedProducts = allProducts.stream()
                .filter(p -> !productDiscountRepository.findByProduct_ProductId(p.getProductId()).isEmpty())
                .toList();

        // Map sang DTO
        return discountedProducts.stream()
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
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        return mapToVariantDTO(variant);
    }

    @Override
    public ProductDTO addProductWithVariants(Long categoryId, ProductCreateDTO createDTO,
                                             MultipartFile mainImage, List<MultipartFile> variantImages) throws IOException {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        String mainImageUrl = fileService.uploadImage("products", mainImage);
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
        savedProduct.setFinalPrice(calculateFinalPrice(savedProduct, null));
        productRepository.save(savedProduct);
        return mapToDTO(savedProduct);
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
    public ProductResponse getProductsByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> page = productRepository.findByCategoryCategoryId(categoryId, pageable);
        List<ProductDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return new ProductResponse(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
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
    public ProductDTO updateProduct(Long productId, ProductUpdateDTO updateDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        // Cập nhật product
        if (updateDTO.getProductName() != null) product.setProductName(updateDTO.getProductName());
        if (updateDTO.getShortDescription() != null) product.setShortDescription(updateDTO.getShortDescription());
        if (updateDTO.getLongDescription() != null) product.setLongDescription(updateDTO.getLongDescription());
        if (updateDTO.getPrice() != null) product.setPrice(updateDTO.getPrice());
        // Cập nhật variants
        if (updateDTO.getVariants() != null) {
            for (VariantUpdateDTO vDto : updateDTO.getVariants()) {
                ProductVariant variant = variantRepository.findById(vDto.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Phiên bản sản phẩm không tồn tại"));

                if (vDto.getColor() != null) variant.setColor(vDto.getColor());
                if (vDto.getStockQuantity() != null) variant.setStockQuantity(vDto.getStockQuantity());
                if (vDto.getPriceOverride() != null) variant.setPriceOverride(vDto.getPriceOverride());
            }
        }
        product.setFinalPrice(calculateFinalPrice(product, null));
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
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Phiên bản sản phẩm không tồn tại"));
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
        Product product = variant.getProduct();
        product.setFinalPrice(calculateFinalPrice(product, null));
        productRepository.save(product);
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
    @Override
    public void updateProductFinalPrice(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));
        product.setFinalPrice(calculateFinalPrice(product, null));
        productRepository.save(product);
    }

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
    // Helper: Map
    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setPrice(product.getPrice());
        dto.setShortDescription(product.getShortDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setFinalPrice(calculateFinalPrice(product, null));
        dto.setImageUrl(fileService.getFullImageUrl(product.getImagePath()));
        // CHUYÊN NGHIỆP: DÙNG ENTITY → KHÔNG CẦN QUERY LẠI
        List<AppliedDiscountDTO> applied = productDiscountRepository
                .findByProduct_ProductId(product.getProductId())
                .stream()
                .map(pd -> mapToAppliedDiscountDTO(pd.getDiscount())) // ← DỰNG SẴN
                .collect(Collectors.toList());

        dto.setAppliedDiscounts(applied);
        dto.setVariants(product.getVariants().stream().map(this::mapToVariantDTO).toList());
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

        // NHƯ PRODUCT: DÙNG ENTITY ĐÃ LOAD → KHÔNG QUERY LẠI
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