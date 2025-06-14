package dev.parcel.orders.controller;

import dev.parcel.orders.model.dao.Order;
import dev.parcel.orders.model.dto.OrderDto;
import dev.parcel.orders.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("create")
    public ResponseEntity<OrderDto> createOrder(@RequestBody Order order) {
        var created = orderService.createOrder(order);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/internal/{orderId}/redelivery-request")
    public ResponseEntity<Void> requestRedelivery(@PathVariable Long orderId) {
        orderService.markAsUnassigned(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/unassigned")
    public ResponseEntity<List<OrderDto>> getUnassignedOrdersByPostalZone(@RequestParam String postalZone) {
        var orders = orderService.getUnassignedOrdersByPostalZone(postalZone);
        return ResponseEntity.ok(OrderDto.convert(orders));
    }

    @PostMapping("/internal/{orderId}/mark-assigned")
    public ResponseEntity<Void> markAssigned(@PathVariable Long orderId) {
        orderService.markAsAssigned(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }
}
