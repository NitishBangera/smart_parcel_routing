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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final OrderService orderService;
    private final DeliveryRepository deliveryRepository;

    public void saveOrderForTruck(long truckId, OrderDto order) {
        var orderId = order.getId();
        var deliveryInformation = new DeliveryInformation();
        deliveryInformation.setOrderId(orderId);
        deliveryInformation.setDeliveryDatetime(OffsetDateTime.now().plusDays(1));
        deliveryInformation.setPinCode(order.getPinCode());
        deliveryInformation.setAddress(order.getAddress());
        deliveryInformation.setPriority(order.getPriority());
        deliveryInformation.setStatus(DeliveryStatus.PENDING);
        deliveryInformation.setTruckId(truckId);
        deliveryInformation.setVolume(order.getVolume());
        deliveryInformation.setWeight(order.getWeight());
        var delivery = deliveryRepository.save(deliveryInformation);
        log.info("Delivery with id {} created for order {} assigned to truck {}", delivery.getId(), orderId, truckId);
    }

    public void processDeliveryResults(Long truckId, DeliveryResultPayload payload) {
        for (var update : payload.getDeliveries()) {
            var info = deliveryRepository.findByIdAndTruckId(update.getDeliveryId(), truckId).orElseThrow();
            info.setStatus(update.getStatus());
            deliveryRepository.save(info);

            if (update.getStatus() == DeliveryStatus.COULD_NOT_DELIVER) {
                var orderId = info.getOrderId();
                log.info("Order {} could not be delivered. Processing redelivery.", orderId);
                // Trigger redelivery
                orderService.redeliveryRequest(orderId);
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
