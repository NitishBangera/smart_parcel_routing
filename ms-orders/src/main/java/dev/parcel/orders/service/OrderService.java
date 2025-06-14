package dev.parcel.orders.service;

import dev.parcel.orders.model.Priority;
import dev.parcel.orders.model.dao.Order;
import dev.parcel.orders.model.dto.OrderDto;
import dev.parcel.orders.repository.OrderRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String TRUCKS_ENDPOINT = "http://truck-service:8082/trucks/internal";

    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate;

    public OrderDto createOrder(Order order) {
        order.setAssigned(false);
        var orderDto = new OrderDto(orderRepository.save(order));

        if (order.getPriority() == Priority.EXPRESS) {
            // Notify truck service for assignment
            CompletableFuture.runAsync(() -> restTemplate.postForEntity(TRUCKS_ENDPOINT + "/assign-order",
                    orderDto, Void.class));
        }

        return orderDto;
    }

    public void markAsUnassigned(long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setAssigned(false);
        orderRepository.save(order);
    }

    public List<Order> getUnassignedOrdersByPostalZone(String postalZone) {
        return orderRepository.findByAssignedFalseAndPinCodeStartingWith(postalZone);
    }

    public void markAsAssigned(long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setAssigned(true);
        orderRepository.save(order);
    }

    public List<OrderDto> getAllOrders() {
        return OrderDto.convert(orderRepository.findAll());
    }
}
