package com.app.coworking.cache;

import com.app.coworking.model.Workspace;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceCache extends LfuCache<Workspace> {
    public WorkspaceCache() {
        super(100);
    }
}
