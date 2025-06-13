package dev.parcel.trucks.controller;

import dev.parcel.trucks.model.DeliveryResultPayload;
import dev.parcel.trucks.model.dao.Truck;
import dev.parcel.trucks.model.dto.OrderDto;
import dev.parcel.trucks.model.dto.TruckDto;
import dev.parcel.trucks.service.TruckService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trucks")
@RequiredArgsConstructor
public class TruckController {
    private final TruckService truckService;

    @PostMapping("/add")
    public TruckDto addTruck(@RequestBody Truck truck) {
        return truckService.addTruck(truck);
    }

    @PostMapping("/{truckId}/return")
    public ResponseEntity<Void> processTruckReturn(@PathVariable Long truckId,
                                                   @RequestBody DeliveryResultPayload payload) {
        truckService.processDeliveryResults(truckId, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/assign-order")
    public ResponseEntity<Void> handleOrderAssignment(@RequestBody OrderDto order) {
        truckService.tryAssignOrder(order);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public List<TruckDto> getAllTrucks() {
        return truckService.getAllTrucks();
    }
}