package dev.parcel.trucks.controller;

import dev.parcel.trucks.model.dto.DeliveryInformationDto;
import dev.parcel.trucks.service.DeliveryService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @GetMapping("/get")
    public List<DeliveryInformationDto> getDeliveries(
            @RequestParam(required = false) Long truckId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return deliveryService.getDeliveries(truckId, date);
    }
}
