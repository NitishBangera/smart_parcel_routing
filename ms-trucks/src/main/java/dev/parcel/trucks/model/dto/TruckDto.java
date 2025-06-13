package dev.parcel.trucks.model.dto;

import dev.parcel.trucks.model.dao.Truck;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class TruckDto {
    private final long id;

    private final double maxWeight;

    private final double maxVolume;

    private final double currentWeight;

    private final double currentVolume;

    private final String postalZone;

    private final OffsetDateTime createdTime;

    public TruckDto(Truck truck) {
        this.id = truck.getId();
        this.maxWeight = truck.getMaxWeight();
        this.maxVolume = truck.getMaxVolume();
        this.currentWeight = truck.getCurrentWeight();
        this.currentVolume = truck.getCurrentVolume();
        this.postalZone = truck.getPostalZone();
        this.createdTime = truck.getCreatedTime();
    }

    public static List<TruckDto> convert(List<Truck> trucks) {
        return trucks.parallelStream().map(TruckDto::new).toList();
    }
}
