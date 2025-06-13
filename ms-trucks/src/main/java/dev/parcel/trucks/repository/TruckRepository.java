package dev.parcel.trucks.repository;

import dev.parcel.trucks.model.dao.Truck;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckRepository extends JpaRepository<Truck, Long> {
    List<Truck> findByPostalZone(String postalZone);
}
