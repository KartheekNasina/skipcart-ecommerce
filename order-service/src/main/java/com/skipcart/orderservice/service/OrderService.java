package com.skipcart.orderservice.service;

import com.skipcart.orderservice.client.ProductServiceClient;
import com.skipcart.orderservice.client.UserServiceClient;
import com.skipcart.orderservice.dto.OrderItemRequestDTO;
import com.skipcart.orderservice.dto.OrderRequestDTO;
import com.skipcart.orderservice.dto.OrderResponseDTO;
import com.skipcart.orderservice.dto.external.ProductDTO;
import com.skipcart.orderservice.dto.external.UserDTO;
import com.skipcart.orderservice.entity.Order;
import com.skipcart.orderservice.entity.OrderItem;
import com.skipcart.orderservice.exception.InvalidOrderStateException;
import com.skipcart.orderservice.exception.OrderNotFoundException;
import com.skipcart.orderservice.exception.UserNotFoundException;
import com.skipcart.orderservice.exception.ProductNotFoundException;
import com.skipcart.orderservice.exception.InsufficientStockException;
import com.skipcart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        log.info("Creating order for userId: {}", dto.getUserId());

        // Step 1: Validate user exists (via WebClient -> user-service)
        UserDTO user = userServiceClient.getUserById(dto.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + dto.getUserId());
        }
        log.info("User validated: {}", user.getEmail());

        Order order = Order.builder()
                .userId(dto.getUserId())
                .shippingAddress(dto.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Step 2: For each item, fetch REAL product data (via RestTemplate -> product-service)
        // We do NOT trust client-provided price/name - always verify against source of truth
        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            ProductDTO product = productServiceClient.getProductById(itemDto.getProductId());

            if (product == null) {
                throw new ProductNotFoundException("Product not found with id: " + itemDto.getProductId());
            }

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ProductNotFoundException("Product is no longer available: " + product.getName());
            }

            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + product.getName() +
                                ". Available: " + product.getStockQuantity() +
                                ", Requested: " + itemDto.getQuantity()
                );
            }

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())       // From product-service, not client input
                    .unitPrice(product.getPrice())          // From product-service, not client input
                    .quantity(itemDto.getQuantity())
                    .build();

            order.addItem(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        log.info("Order created with id: {}, total: {}", saved.getId(), saved.getTotalAmount());

        return OrderResponseDTO.fromEntity(saved);
    }

    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return OrderResponseDTO.fromEntity(order);
    }

    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", id, newStatus);

        return OrderResponseDTO.fromEntity(updated);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        log.info("Order {} cancelled", id);

        return OrderResponseDTO.fromEntity(updated);
    }

    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        if (current == Order.OrderStatus.CANCELLED || current == Order.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    "Cannot change status of an order that is already " + current);
        }
    }
}