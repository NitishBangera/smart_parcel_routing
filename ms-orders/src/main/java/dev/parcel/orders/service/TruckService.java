package dev.parcel.orders.service;

import dev.parcel.orders.model.dto.OrderDto;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TruckService {
    private static final String TRUCKS_ENDPOINT = "http://truck-service:8082/trucks/internal";

    private final RestTemplate restTemplate;

    public void assignOrder(OrderDto orderDto) {
        CompletableFuture.runAsync(() -> restTemplate.postForEntity(TRUCKS_ENDPOINT + "/assign-order",
                orderDto, Void.class));
    }
}
