package com.distsystem.base.dtos;

import com.distsystem.api.AgentSimplified;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/** row for JDBC table distagentregister */
public class DistAgentRegisterRow {

    public LocalDateTime createDate;
    public String agentguid;
    public String hostname;
    public String hostip;
    public int portnumber;
    public LocalDateTime lastpingdate;
    public int isactive;

    public DistAgentRegisterRow() {
    }
    public DistAgentRegisterRow(LocalDateTime createDate, String agentguid, String hostname, String hostip, int portnumber, LocalDateTime lastpingdate, int isactive) {
        this.createDate = createDate;
        this.agentguid = agentguid;
        this.hostname = hostname;
        this.hostip = hostip;
        this.portnumber = portnumber;
        this.lastpingdate = lastpingdate;
        this.isactive = isactive;
    }
    /** */
    public void deactivate() {
        isactive = 0;
    }
    /** */
    public AgentSimplified toSimplified() {
        return new AgentSimplified(agentguid, hostname, hostip, portnumber, lastpingdate);
    }
    public Map<String, String> toMap() {
        if (isactive==0) {
            return Map.of("type", "agent",
                    "isactive", ""+isactive,
                    "agentguid", agentguid,
                    "hostname", hostname,
                    "hostip", hostip,
                    "portnumber", "" + portnumber,
                    "lastpingdate", lastpingdate.toString(),
                    "createDate", createDate.toString(),
                    "closedate", LocalDateTime.now().toString());
        } else {
            return Map.of("type", "agent",
                    "isactive", ""+isactive,
                    "agentguid", agentguid,
                    "hostname", hostname,
                    "hostip", hostip,
                    "portnumber", "" + portnumber,
                    "lastpingdate", lastpingdate.toString(),
                    "createDate", createDate.toString());
        }
    }
    public static DistAgentRegisterRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentRegisterRow(
                m.getLocalDateOrNow("createDate"),
                m.getString("agentguid", ""),
                m.getString("hostname", ""),
                m.getString("hostip", ""),
                m.getInt("portnumber", 8085),
                m.getLocalDateOrNow("lastpingdate"),
                m.getInt("isactive", 1)
        );
    }
    public void ping(LocalDateTime pingdate) {
        this.lastpingdate = pingdate;
    }
    public String getAgentguid() {
        return agentguid;
    }
    public String getHostname() {
        return hostname;
    }
    public String getHostip() {
        return hostip;
    }
    public int getPortnumber() {
        return portnumber;
    }
    public LocalDateTime getLastpingdate() {
        return lastpingdate;
    }
    public int getIsactive() {
        return isactive;
    }

    public String toString() {
        return "agentguid=" + agentguid + ", hostname=" + hostname + ", hostip=" + hostip + ", portnumber=" + portnumber + ", lastpingdate=" + lastpingdate + "";
    }
}
