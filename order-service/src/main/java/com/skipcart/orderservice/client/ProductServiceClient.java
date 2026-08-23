package com.skipcart.orderservice.client;

import com.skipcart.orderservice.dto.external.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://product-service/products";

    public ProductDTO getProductById(String productId) {
        try {
            return restTemplate.getForObject(
                    PRODUCT_SERVICE_URL + "/{id}",
                    ProductDTO.class,
                    productId
            );
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Product not found with id: {}", productId);
            return null;
        } catch (Exception ex) {
            log.error("Error calling product-service: {}", ex.getMessage());
            throw new RuntimeException("Unable to reach product-service: " + ex.getMessage());
        }
    }
}