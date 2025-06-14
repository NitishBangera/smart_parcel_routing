package dev.parcel.trucks.model.dao;

import dev.parcel.trucks.model.DeliveryStatus;
import dev.parcel.trucks.model.Priority;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long orderId;

    private String address;

    private String pinCode;

    private double weight;

    private double volume;

    private OffsetDateTime deliveryDatetime;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private Long truckId;
}