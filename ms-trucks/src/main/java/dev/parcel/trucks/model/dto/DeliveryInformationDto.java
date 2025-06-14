package dev.parcel.trucks.model.dto;

import dev.parcel.trucks.model.DeliveryStatus;
import dev.parcel.trucks.model.Priority;
import dev.parcel.trucks.model.dao.DeliveryInformation;
import dev.parcel.trucks.model.dao.Truck;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class DeliveryInformationDto {
    private final long id;

    private final long orderId;

    private final String address;

    private final String pinCode;

    private final LocalDate deliveryDate;

    private final DeliveryStatus status;

    private final Priority priority;

    public DeliveryInformationDto(DeliveryInformation deliveryInformation) {
        this.id = deliveryInformation.getId();
        this.orderId = deliveryInformation.getOrderId();
        this.address = deliveryInformation.getAddress();
        this.pinCode = deliveryInformation.getPinCode();
        this.deliveryDate = deliveryInformation.getDeliveryDatetime().toLocalDate();
        this.status = deliveryInformation.getStatus();
        this.priority = deliveryInformation.getPriority();
    }

    public static List<DeliveryInformationDto> convert(List<DeliveryInformation> deliveries) {
        return deliveries.parallelStream().map(DeliveryInformationDto::new).toList();
    }
}
