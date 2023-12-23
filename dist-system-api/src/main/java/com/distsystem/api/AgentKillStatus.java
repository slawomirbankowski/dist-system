package com.distsystem.api;

import com.distsystem.api.info.*;
import java.time.LocalDateTime;

/** */
public class AgentKillStatus {

    private String agentGuid;
    private LocalDateTime createDate;
    private LocalDateTime killedDate;


    public AgentKillStatus(String agentGuid,
                     LocalDateTime createDate, LocalDateTime killedDate) {
        this.agentGuid = agentGuid;
        this.createDate = createDate;
        this.killedDate = killedDate;
    }
    public String getAgentGuid() {
        return agentGuid;
    }
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    public LocalDateTime getKilledDate() {
        return killedDate;
    }
}
