package dev.parcel.trucks.service;

import dev.parcel.trucks.model.dto.OrderDto;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String ORDER_ENDPOINT = "http://order-service:8081/orders/internal";

    private final RestTemplate restTemplate;

    public void markAssigned(long orderId) {
        CompletableFuture.runAsync(() -> restTemplate.postForEntity(
                ORDER_ENDPOINT + "/" + orderId + "/mark-assigned",
                null, Void.class));
    }

    public List<OrderDto> markUnassigned(String postalZone) {
        var response = restTemplate.getForEntity(
                ORDER_ENDPOINT + "/unassigned/" + postalZone,
                OrderDto[].class).getBody();

        return response != null ? Arrays.asList(response) : null;
    }

    public void redeliveryRequest(long orderId) {
        CompletableFuture.runAsync(() -> restTemplate.postForEntity(
                ORDER_ENDPOINT + "/" + orderId + "/redelivery-request",
                null,
                Void.class));
    }
}
