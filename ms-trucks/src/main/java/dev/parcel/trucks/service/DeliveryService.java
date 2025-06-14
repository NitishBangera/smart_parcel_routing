package dev.parcel.trucks.service;

import dev.parcel.trucks.model.DeliveryResultPayload;
import dev.parcel.trucks.model.DeliveryStatus;
import dev.parcel.trucks.model.dao.DeliveryInformation;
import dev.parcel.trucks.model.dto.DeliveryInformationDto;
import dev.parcel.trucks.model.dto.OrderDto;
import dev.parcel.trucks.repository.DeliveryRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private static final String ORDER_ENDPOINT = "http://order-service:8081/orders/internal";
    private final DeliveryRepository deliveryRepository;
    private final RestTemplate restTemplate;

    public void saveOrderForTruck(long truckId, OrderDto order) {
        var deliveryInformation = new DeliveryInformation();
        deliveryInformation.setOrderId(order.getId());
        deliveryInformation.setDeliveryDatetime(OffsetDateTime.now().plusDays(1));
        deliveryInformation.setPinCode(order.getPinCode());
        deliveryInformation.setAddress(order.getAddress());
        deliveryInformation.setPriority(order.getPriority());
        deliveryInformation.setStatus(DeliveryStatus.PENDING);
        deliveryInformation.setTruckId(truckId);
        deliveryInformation.setVolume(order.getVolume());
        deliveryInformation.setWeight(order.getWeight());
        deliveryRepository.save(deliveryInformation);
    }

    public void processDeliveryResults(Long truckId, DeliveryResultPayload payload) {
        for (var update : payload.getDeliveries()) {
            var info = deliveryRepository.findByIdAndTruckId(update.getDeliveryId(), truckId).orElseThrow();
            info.setStatus(update.getStatus());
            deliveryRepository.save(info);

            if (update.getStatus() == DeliveryStatus.COULD_NOT_DELIVER) {
                // Trigger redelivery
                CompletableFuture.runAsync(() -> restTemplate.postForEntity(
                        ORDER_ENDPOINT + "/" + info.getOrderId() + "/redelivery-request",
                        null,
                        Void.class));
            }
        }
    }

    public List<DeliveryInformationDto> getDeliveries(Long truckId, LocalDate date) {
        OffsetDateTime start;
        OffsetDateTime end;

        if (date != null) {
            // If a date is given, use that day only
            start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            end = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        } else {
            // If no date is given, fetch from 7 days ago until now
            start = LocalDate.now().minusDays(7).atStartOfDay().atOffset(ZoneOffset.UTC);
            end = OffsetDateTime.now(ZoneOffset.UTC);
        }

        log.info("Deliveries for Truck Id : {}, Start : {}, End : {}", truckId, start, end);

        var deliveries = truckId != null
                ? deliveryRepository.findByTruckIdAndDeliveryDatetimeBetween(truckId, start, end)
                : deliveryRepository.findByDeliveryDatetimeBetween(start, end);

        return DeliveryInformationDto.convert(deliveries.orElse(new LinkedList<>()));
    }
}
