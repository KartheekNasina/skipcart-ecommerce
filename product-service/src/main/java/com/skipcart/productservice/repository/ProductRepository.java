package com.skipcart.productservice.repository;

import com.skipcart.productservice.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findByVendorId(Long vendorId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByActiveTrue();
}