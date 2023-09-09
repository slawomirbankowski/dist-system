package com.distsystem.base.dtos;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentServiceRow {

    public String agentguid;

    public String serviceguid;
    public String servicetype;
    public LocalDateTime createddate;
    public int isactive;
    public LocalDateTime lastpingdate;

    /** */
    public DistAgentServiceRow() {
    }

    public DistAgentServiceRow(String agentguid, String serviceguid, String servicetype, LocalDateTime createddate, int isactive, LocalDateTime lastpingdate) {
        this.agentguid = agentguid;
        this.serviceguid = serviceguid;
        this.servicetype = servicetype;
        this.createddate = createddate;
        this.isactive = isactive;
        this.lastpingdate = lastpingdate;
    }

    public String getAgentguid() {
        return agentguid;
    }

    public String getServiceguid() {
        return serviceguid;
    }

    public String getServicetype() {
        return servicetype;
    }

    public LocalDateTime getCreateddate() {
        return createddate;
    }

    public int getIsactive() {
        return isactive;
    }

    public LocalDateTime getLastpingdate() {
        return lastpingdate;
    }

    public Map<String, String> toMap() {
        return Map.of("type", "service",
                "agentguid", agentguid,
                "serviceguid", serviceguid,
                "servicetype", servicetype,
                "isactive", "" + isactive,
                "createddate", createddate.toString(),
                "lastpingdate", lastpingdate.toString());
    }

}
