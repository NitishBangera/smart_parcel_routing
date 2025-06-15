package dev.parcel.orders.service;

import dev.parcel.orders.model.Priority;
import dev.parcel.orders.model.dao.Order;
import dev.parcel.orders.model.dto.OrderDto;
import dev.parcel.orders.repository.OrderRepository;
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
        log.info("Order {} created", orderDto.getId());
        return orderDto;
    }

    public void markAsUnassigned(long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setAssigned(false);
        orderRepository.save(order);
        log.info("Order id {} marked unassigned", orderId);
    }

    public List<OrderDto> getUnassignedOrdersByPostalZone(String postalZone) {
        var orders = orderRepository.findByAssignedFalseAndPinCodeStartingWith(postalZone);
        log.info("{} orders unassigned", orders.size());
        return OrderDto.convert(orders);
    }

    public void markAsAssigned(long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setAssigned(true);
        orderRepository.save(order);
        log.info("Order {} assigned to truck", orderId);
    }

    public List<OrderDto> getAllOrders() {
        return OrderDto.convert(orderRepository.findAll());
    }
}
