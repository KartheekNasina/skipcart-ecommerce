package com.skipcart.orderservice.controller;

import com.skipcart.orderservice.dto.OrderRequestDTO;
import com.skipcart.orderservice.dto.OrderResponseDTO;
import com.skipcart.orderservice.entity.Order;
import com.skipcart.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO response = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}