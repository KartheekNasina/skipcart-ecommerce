package com.skipcart.orderservice.dto.external;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean active;
}