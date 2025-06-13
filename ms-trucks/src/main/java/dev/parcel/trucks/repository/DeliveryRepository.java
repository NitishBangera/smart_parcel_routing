package dev.parcel.trucks.repository;

import dev.parcel.trucks.model.dao.DeliveryInformation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryInformation, Long> {
    Optional<DeliveryInformation> findByIdAndTruckId(long deliveryId, long truckId);
}
