package com.skipcart.productservice.service;

import com.skipcart.productservice.dto.ProductRequestDTO;
import com.skipcart.productservice.dto.ProductResponseDTO;
import com.skipcart.productservice.entity.Product;
import com.skipcart.productservice.exception.ProductNotFoundException;
import com.skipcart.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        log.info("Creating product: {} (cache evicted)", dto.getName());

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .category(dto.getCategory())
                .vendorId(dto.getVendorId())
                .imageUrl(dto.getImageUrl())
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());

        return ProductResponseDTO.fromEntity(saved);
    }

    public List<ProductResponseDTO> getAllProducts() {
        log.info("Fetching all products from DATABASE (cache miss)");
        return productRepository.findByActiveTrue()
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    @Cacheable(value = "product", key = "#id")
    public ProductResponseDTO getProductById(String id) {
        log.info("Fetching product {} from DATABASE (cache miss)", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return ProductResponseDTO.fromEntity(product);
    }

    public List<ProductResponseDTO> getProductsByCategory(String category) {
        // Not cached - too many variations (category combinations), simpler to just query DB
        return productRepository.findByCategory(category)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    public List<ProductResponseDTO> searchProductsByName(String name) {
        // Not cached - search queries are highly variable, low cache hit rate expected
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }
    @CacheEvict(value = "product", key = "#id")
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO dto) {
        log.info("Updating product {} (cache evicted)", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());

        return ProductResponseDTO.fromEntity(updated);
    }
    @CacheEvict(value = "product", key = "#id")
    public void deleteProduct(String id) {
        log.info("Deleting product {} (cache evicted)", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }
}