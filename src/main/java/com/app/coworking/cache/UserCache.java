package com.app.coworking.cache;

import com.app.coworking.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserCache extends LfuCache<User> {
    public UserCache() {
        super(100);
    }
}
