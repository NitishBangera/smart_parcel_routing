package dev.parcel.trucks.service;

import dev.parcel.trucks.model.DeliveryResultPayload;
import dev.parcel.trucks.model.dao.Truck;
import dev.parcel.trucks.model.dto.OrderDto;
import dev.parcel.trucks.model.dto.TruckDto;
import dev.parcel.trucks.repository.TruckRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TruckService {
    private final DeliveryService deliveryService;
    private final OrderService orderService;
    private final TruckRepository truckRepository;

    public TruckDto addTruck(Truck truck) {
        var truckDto = new TruckDto(truckRepository.save(truck));
        log.info("Truck {} added", truckDto.getId());
        return truckDto;
    }

    public void processDeliveryResults(Long truckId, DeliveryResultPayload payload) {
        deliveryService.processDeliveryResults(truckId, payload);
    }

    public void tryAssignOrder(OrderDto order) {
        // Find suitable truck in same postal zone
        var postalZone = order.getPinCode().substring(0, 3);
        var trucks = truckRepository.findByPostalZone(postalZone);
        if (trucks.isEmpty()) return;

        var truck = trucks.stream()
                .filter(t -> t.getCurrentWeight() + order.getWeight() <= t.getMaxWeight())
                .filter(t -> t.getCurrentVolume() + order.getVolume() <= t.getMaxVolume())
                .min(Comparator.comparing(t -> t.getCurrentWeight() + t.getCurrentVolume())).orElse(null);

        var orderId = order.getId();
        if (truck != null) {
            var truckId = truck.getId();
            deliveryService.saveOrderForTruck(truckId, order);

            log.info("Order {} assigned to truck {}", orderId, truckId);
            // Notify order service the order is assigned
            orderService.markAssigned(orderId);

            truck.setCurrentVolume(truck.getCurrentVolume() + order.getVolume());
            truck.setCurrentWeight(truck.getCurrentWeight() + order.getWeight());
            truckRepository.save(truck);
        } else {
            log.info("No trucks available. Order {} could not be assigned.", orderId);
        }
    }

    public List<TruckDto> getAllTrucks() {
        return TruckDto.convert(truckRepository.findAll());
    }

    public void scheduledAssignUnassigned() {
        var zones = truckRepository.findAll()
                .stream()
                .map(Truck::getPostalZone)
                .distinct()
                .toList();
        for (String zone : zones) {
            assignUnassignedOrders(zone);
        }
    }

    public void assignUnassignedOrders(String postalZone) {
        var orders = orderService.markUnassigned(postalZone);

        if (orders == null) return;

        log.info("{} orders unassigned. Processing.", orders.size());
        orders.sort(Comparator.comparing(OrderDto::getPriority).reversed());
        orders.forEach(this::tryAssignOrder);
    }
}
