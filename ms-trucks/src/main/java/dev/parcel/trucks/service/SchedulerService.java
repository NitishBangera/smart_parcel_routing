package dev.parcel.trucks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService {
    private final TruckService truckService;

    // @Scheduled(cron = "0 */15 0,6-23 * * *", zone = "Asia/Kolkata")
    //@Scheduled(cron = "0 */5 6-23 * * *", zone = "Asia/Kolkata")
    @Scheduled(cron = "0 0 6-23,0 * * *", zone = "Asia/Kolkata")
    public void scheduledAssignUnassigned() {
        truckService.scheduledAssignUnassigned();
    }
}
