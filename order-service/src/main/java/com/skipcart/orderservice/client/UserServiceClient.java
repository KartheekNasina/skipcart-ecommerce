package com.skipcart.orderservice.client;

import com.skipcart.orderservice.dto.external.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    private static final String USER_SERVICE_URL = "http://user-service/users";

    public UserDTO getUserById(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(USER_SERVICE_URL + "/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block(); // .block() makes this call synchronous - fine for our servlet-based flow
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("User not found with id: {}", userId);
            return null;
        } catch (Exception ex) {
            log.error("Error calling user-service: {}", ex.getMessage());
            throw new RuntimeException("Unable to reach user-service: " + ex.getMessage());
        }
    }
}