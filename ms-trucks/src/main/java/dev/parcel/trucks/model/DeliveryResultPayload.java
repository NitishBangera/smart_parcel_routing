package dev.parcel.trucks.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryResultPayload {
    private OffsetDateTime dateTime;
    private List<DeliveryUpdate> deliveries;

    @Data
    public static class DeliveryUpdate {
        private Long deliveryId;
        private DeliveryStatus status;
    }
}
