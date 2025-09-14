package com.app.coworking.cache;

import com.app.coworking.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationCache extends LfuCache<Reservation> {
    public ReservationCache() {
        super(100);
    }
}
