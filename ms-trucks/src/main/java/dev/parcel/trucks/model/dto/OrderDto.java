package dev.parcel.trucks.model.dto;

import dev.parcel.trucks.model.Priority;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto {
    private long id;

    private String address;

    private String pinCode;

    private double weight;

    private double volume;

    private Priority priority;

    private OffsetDateTime createdTime;

    private boolean assigned;
}
