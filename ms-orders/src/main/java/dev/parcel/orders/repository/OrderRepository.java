package dev.parcel.orders.repository;

import dev.parcel.orders.model.dao.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAssignedFalseAndPinCodeStartingWith(String postalZone);
}
