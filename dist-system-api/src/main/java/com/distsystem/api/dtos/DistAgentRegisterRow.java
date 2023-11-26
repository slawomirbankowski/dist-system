package com.distsystem.api.dtos;

import com.distsystem.api.AgentSimplified;
import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.Map;

/** row for JDBC table distagentregister */
@DaoTable(tableName="DistAgentRegister", keyName="agentGuid", keyIsUnique=true)
public class DistAgentRegisterRow extends BaseRow {

    private String registerGuid;
    private LocalDateTime createDate;
    private String agentGuid;
    private String hostName;
    private String hostIp;
    private int portNumber;
    private LocalDateTime lastPingDate;

    protected int isActive;
    protected LocalDateTime createdDate;
    protected LocalDateTime lastUpdatedDate;

    private int active;

    public DistAgentRegisterRow() {
    }
    public DistAgentRegisterRow(LocalDateTime createDate, String agentGuid, String hostName, String hostip, int portnumber, LocalDateTime lastpingdate, int active) {
        this.registerGuid = DistUtils.generateCustomGuid("REGISTER");
        this.createDate = createDate;
        this.agentGuid = agentGuid;
        this.hostName = hostName;
        this.hostIp = hostip;
        this.portNumber = portnumber;
        this.lastPingDate = lastpingdate;
        this.active = active;
    }

    /** */
    public void deactivate() {
        active = 0;
    }
    /** */
    public AgentSimplified toSimplified() {
        return new AgentSimplified(agentGuid, hostName, hostIp, portNumber, lastPingDate);
    }
    public Map<String, String> toMap() {
        if (active ==0) {
            return Map.of("type", "agent",
                    "isactive", ""+ active,
                    "agentguid", agentGuid,
                    "hostname", hostName,
                    "hostip", hostIp,
                    "portnumber", "" + portNumber,
                    "lastpingdate", lastPingDate.toString(),
                    "createDate", createDate.toString(),
                    "closedate", LocalDateTime.now().toString());
        } else {
            return Map.of("type", "agent",
                    "isactive", ""+ active,
                    "agentguid", agentGuid,
                    "hostname", hostName,
                    "hostip", hostIp,
                    "portnumber", "" + portNumber,
                    "lastpingdate", lastPingDate.toString(),
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
        this.lastPingDate = pingdate;
    }
    public String getAgentGuid() {
        return agentGuid;
    }
    public String getHostName() {
        return hostName;
    }
    public String getHostIp() {
        return hostIp;
    }
    public int getPortNumber() {
        return portNumber;
    }
    public LocalDateTime getLastPingDate() {
        return lastPingDate;
    }
    public int getActive() {
        return active;
    }

    public String getRegisterGuid() {
        return registerGuid;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }


    public int getIsActive() {
        return isActive;
    }


    public LocalDateTime getCreatedDate() {
        return createdDate;
    }


    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { registerGuid, createDate, agentGuid, hostName, hostIp, portNumber, lastPingDate, active };
    }


    public String toString() {
        return "agentguid=" + agentGuid + ", hostname=" + hostName + ", hostip=" + hostIp + ", portnumber=" + portNumber + ", lastpingdate=" + lastPingDate + "";
    }
}
