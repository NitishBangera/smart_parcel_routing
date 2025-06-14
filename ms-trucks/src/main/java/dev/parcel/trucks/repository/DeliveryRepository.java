package dev.parcel.trucks.repository;

import dev.parcel.trucks.model.DeliveryStatus;
import dev.parcel.trucks.model.dao.DeliveryInformation;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryInformation, Long> {
    Optional<DeliveryInformation> findByIdAndTruckId(long deliveryId, long truckId);

    Optional<List<DeliveryInformation>> findByTruckIdAndDeliveryDatetimeBetween(Long truckId, OffsetDateTime start, OffsetDateTime end);

    Optional<List<DeliveryInformation>> findByDeliveryDatetimeBetween(OffsetDateTime start, OffsetDateTime end);
}
