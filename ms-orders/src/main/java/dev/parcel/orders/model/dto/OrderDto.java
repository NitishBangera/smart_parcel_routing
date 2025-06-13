package dev.parcel.orders.model.dto;

import dev.parcel.orders.model.Priority;
import dev.parcel.orders.model.dao.Order;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class OrderDto {
    private final long id;

    private final String address;

    private final String pinCode;

    private final double weight;

    private final double volume;

    private final Priority priority;

    private final OffsetDateTime createdTime;

    private final boolean assigned;

    public OrderDto(Order order) {
        this.id = order.getId();
        this.address = order.getAddress();
        this.pinCode = order.getPinCode();
        this.weight = order.getWeight();
        this.volume = order.getVolume();
        this.priority = order.getPriority();
        this.assigned = order.isAssigned();
        this.createdTime = order.getCreatedTime();
    }

    public static List<OrderDto> convert(List<Order> orders) {
        return orders.parallelStream().map(OrderDto::new).toList();
    }
}
