package com.distsystem.api.info;

import java.time.LocalDateTime;

/**
 * */
public class AgentSemaphoreManagerInfo {

    private final LocalDateTime createDate;

    public AgentSemaphoreManagerInfo(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getCreateDate() {
        return createDate.toString();
    }
}
