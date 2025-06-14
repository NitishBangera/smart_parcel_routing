package dev.parcel.trucks.service;

import dev.parcel.trucks.model.DeliveryResultPayload;
import dev.parcel.trucks.model.DeliveryStatus;
import dev.parcel.trucks.model.dao.Truck;
import dev.parcel.trucks.model.dto.OrderDto;
import dev.parcel.trucks.model.dto.TruckDto;
import dev.parcel.trucks.repository.TruckRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TruckService {
    private static final String ORDER_ENDPOINT = "http://order-service:8081/orders/internal";
    private final DeliveryService deliveryService;
    private final TruckRepository truckRepository;
    private final RestTemplate restTemplate;

    @Scheduled(fixedRate = 15 * 60 * 1000)
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

    public TruckDto addTruck(Truck truck) {
        return new TruckDto(truckRepository.save(truck));
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
            deliveryService.saveOrderForTruck(truck.getId(), order);

            // Notify order service the order is assigned
            CompletableFuture.runAsync(() -> restTemplate.postForEntity(
                    ORDER_ENDPOINT + "/" + order.getId() + "/mark-assigned",
                    null, Void.class));

            truck.setCurrentVolume(truck.getCurrentVolume() + order.getVolume());
            truck.setCurrentWeight(truck.getCurrentWeight() + order.getWeight());
            truckRepository.save(truck);
        }
    }

    public void assignUnassignedOrders(String postalZone) {
        var response = restTemplate.getForEntity(
                ORDER_ENDPOINT + "/unassigned/" + postalZone,
                OrderDto[].class);

        if (response.getBody() == null) return;

        var orders = Arrays.asList(response.getBody());
        orders.sort(Comparator.comparing(OrderDto::getPriority).reversed());
        orders.forEach(this::tryAssignOrder);
    }

    public List<TruckDto> getAllTrucks() {
        return TruckDto.convert(truckRepository.findAll());
    }
}
