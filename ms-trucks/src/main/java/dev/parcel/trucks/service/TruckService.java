package dev.parcel.trucks.service;

import dev.parcel.trucks.model.DeliveryResultPayload;
import dev.parcel.trucks.model.dao.Truck;
import dev.parcel.trucks.model.dto.OrderDto;
import dev.parcel.trucks.model.dto.TruckDto;
import dev.parcel.trucks.repository.TruckRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TruckService {
    private static final String ORDER_ENDPOINT = "http://order-service:8081/orders/internal";
    private final DeliveryService deliveryService;
    private final TruckRepository truckRepository;
    private final RestTemplate restTemplate;

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

        if (truck != null) {
            var truckId = truck.getId();
            var orderId = order.getId();
            deliveryService.saveOrderForTruck(truckId, order);

            log.info("Order {} assigned to truck {}", orderId, truckId);
            // Notify order service the order is assigned
            CompletableFuture.runAsync(() -> restTemplate.postForEntity(
                    ORDER_ENDPOINT + "/" + orderId + "/mark-assigned",
                    null, Void.class));

            truck.setCurrentVolume(truck.getCurrentVolume() + order.getVolume());
            truck.setCurrentWeight(truck.getCurrentWeight() + order.getWeight());
            truckRepository.save(truck);
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
        var response = restTemplate.getForEntity(
                ORDER_ENDPOINT + "/unassigned/" + postalZone,
                OrderDto[].class);

        if (response.getBody() == null) return;

        var orders = Arrays.asList(response.getBody());
        log.info("{} orders unassigned. Processing.", orders.size());
        orders.sort(Comparator.comparing(OrderDto::getPriority).reversed());
        orders.forEach(this::tryAssignOrder);
    }
}
