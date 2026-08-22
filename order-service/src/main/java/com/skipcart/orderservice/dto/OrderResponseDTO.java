package com.skipcart.orderservice.dto;

import com.skipcart.orderservice.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponseDTO fromEntity(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(order.getItems().stream()
                        .map(OrderItemResponseDTO::fromEntity)
                        .toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}