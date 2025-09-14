package com.app.coworking.cache;

import com.app.coworking.model.Coworking;
import org.springframework.stereotype.Component;

@Component
public class CoworkingCache extends LfuCache<Coworking> {
    public CoworkingCache() {
        super(100);
    }
}
