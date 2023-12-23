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
    private String agentGuid;
    private String hostName;
    private String hostIp;
    private int portNumber;
    private LocalDateTime lastPingDate;
    private long pingsCount;
    private long agentsConnected;
    private long threadsCount;
    private long servicesCount;
    private long serversCount;
    private long clientsCount;
    protected LocalDateTime closeDate;
    private int isActive;
    protected LocalDateTime createdDate;
    protected LocalDateTime lastUpdatedDate;

    public DistAgentRegisterRow(String registerGuid, String agentGuid, String hostName, String hostip, int portnumber, LocalDateTime lastpingdate, LocalDateTime closeDate, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.registerGuid = registerGuid;
        this.agentGuid = agentGuid;
        this.hostName = hostName;
        this.hostIp = hostip;
        this.portNumber = portnumber;
        this.lastPingDate = lastpingdate;
        this.pingsCount = 0L;
        this.agentsConnected = 0L;
        this.threadsCount = 0L;
        this.servicesCount = 0L;
        this.serversCount = 0L;
        this.clientsCount = 0L;
        this.isActive = isActive;
        this.closeDate = closeDate;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentRegisterRow(String agentGuid, String hostName, String hostip, int portnumber) {
        this.registerGuid = DistUtils.generateCustomGuid("REGISTER");
        this.agentGuid = agentGuid;
        this.hostName = hostName;
        this.hostIp = hostip;
        this.portNumber = portnumber;
        this.lastPingDate = LocalDateTime.now();
        this.pingsCount = 0L;
        this.agentsConnected = 0L;
        this.threadsCount = 0L;
        this.servicesCount = 0L;
        this.serversCount = 0L;
        this.clientsCount = 0L;
        this.closeDate = LocalDateTime.of(2049, 12, 31, 23, 59);
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
    }

    /** */
    public void deactivate() {
        isActive = 0;
    }
    /** */
    public AgentSimplified toSimplified() {
        return new AgentSimplified(agentGuid, hostName, hostIp, portNumber, lastPingDate);
    }
    public Map<String, String> toMap() {
        if (isActive ==0) {
            return Map.of("type", "agent",
                    "isactive", ""+ isActive,
                    "agentguid", agentGuid,
                    "hostname", hostName,
                    "hostip", hostIp,
                    "portnumber", "" + portNumber,
                    "lastpingdate", lastPingDate.toString(),
                    "closedate", LocalDateTime.now().toString());
        } else {
            return Map.of("type", "agent",
                    "isactive", ""+ isActive,
                    "agentguid", agentGuid,
                    "hostname", hostName,
                    "hostip", hostIp,
                    "portnumber", "" + portNumber,
                    "lastpingdate", lastPingDate.toString());
        }
    }
    public static DistAgentRegisterRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentRegisterRow(
                m.getString("registerGuid", ""),
                m.getString("agentGuid", ""),
                m.getString("hostName", ""),
                m.getString("hostIp", ""),
                m.getInt("portNumber", 8085),
                m.getLocalDateOrNow("lastPingDate"),
                m.getLocalDateOrNull("closeDate"),
                m.getInt("isActive", 1),
                m.getLocalDateOrNow("createdDate"),
                m.getLocalDateOrNow("lastUpdatedDate")
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
        return isActive;
    }
    public String getRegisterGuid() {
        return registerGuid;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public long getPingsCount() {
        return pingsCount;
    }
    public long getAgentsConnected() {
        return agentsConnected;
    }
    public long getThreadsCount() {
        return threadsCount;
    }
    public long getServicesCount() {
        return servicesCount;
    }
    public long getServersCount() {
        return serversCount;
    }
    public long getClientsCount() {
        return clientsCount;
    }
    public int getIsActive() {
        return isActive;
    }

    public Object[] toInsertRow() {
        return new Object[] { registerGuid, agentGuid, hostName, hostIp, portNumber, lastPingDate,
                pingsCount, agentsConnected, threadsCount, servicesCount, serversCount, clientsCount,
                closeDate, isActive, createdDate, lastUpdatedDate };
    }

    public String toString() {
        return "agentguid=" + agentGuid + ", hostname=" + hostName + ", hostip=" + hostIp + ", portnumber=" + portNumber + ", lastpingdate=" + lastPingDate + "";
    }
}
