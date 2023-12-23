package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** row for JDBC table distagentregister
 * create table distagentserver(agentguid text, servertype text, serverhost text, serverip text, serverport int, serverurl text, createddate timestamp, isactive int, lastpingdate timestamp)
 * */
public class DistAgentServerRow extends BaseRow {

    private String agentGuid;
    private String serverGuid;
    private String serverType;
    private String serverHost;
    private String serverIp;
    private int serverPort;
    private String serverUrl;
    private String serverParams;
    private LocalDateTime lastPingDate;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    /** */
    public DistAgentServerRow(String agentGuid, String serverGuid, String serverType, String serverHost, String serverIp, int serverPort, String serverUrl,
                              LocalDateTime createdDate, int isActive, LocalDateTime lastPingDate, String serverParams) {
        this.agentGuid = agentGuid;
        this.serverGuid = serverGuid;
        this.serverType = serverType;
        this.serverHost = serverHost;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.serverUrl = serverUrl;
        this.lastPingDate = lastPingDate;
        this.serverParams = serverParams;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentServerRow(String agentGuid) {
        this.agentGuid = agentGuid;
        this.serverGuid = "";
        this.serverType = "";
        this.serverHost = "";
        this.serverIp = "";
        this.serverPort = 0;
        this.serverUrl = "";
        this.lastPingDate = LocalDateTime.now();
        this.serverParams = "";
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
    }
    /** simple information about this server */
    public String simpleInfo() {
        return  "(guid: "+ serverGuid + ",agentguid:" + agentGuid + ",type:" + serverType + ",url:" + serverUrl + ", host: " + serverHost + ", port: " + serverPort + ")";
    }
    public void deactivate() {
        this.isActive = 0;
    }
    public DistAgentServerRow copyNoPassword() {
        return new DistAgentServerRow(agentGuid, serverGuid, serverType, serverHost, serverIp, serverPort, serverUrl, createdDate, isActive, lastPingDate, serverParams);
    }
    public String getAgentGuid() {
        return agentGuid;
    }
    public String getServerGuid() {
        return serverGuid;
    }
    public String getServerType() {
        return serverType;
    }
    public String getServerHost() {
        return serverHost;
    }
    public String getServerIp() {
        return serverIp;
    }
    public int getServerPort() {
        return serverPort;
    }
    public String getServerUrl() {
        return serverUrl;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public int getIsActive() {
        return isActive;
    }
    public LocalDateTime getLastPingDate() {
        return lastPingDate;
    }
    public String getServerParams() {
        return serverParams;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] {agentGuid, serverGuid, serverType, serverHost, serverIp,
                serverPort, serverUrl, serverParams, lastPingDate, isActive, createdDate, lastPingDate };
    }
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.putAll(Map.of("type", "server",
                "agentguid", agentGuid,
                "serverguid", serverGuid,
                "servertype", serverType,
                "serverhost", serverHost,
                "serverip", serverIp,
                "serverport", "" + serverPort,
                "serverurl", serverUrl,
                "createddate", createdDate.toString(),
                "lastpingdate", lastPingDate.toString()));
        map.putAll(Map.of("isactive", ""+ isActive,
                "serverparams", serverParams));
        return map;
    }
    public static DistAgentServerRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentServerRow(
                m.getString("agentguid", ""),
                m.getString("serverguid", ""),
                m.getString("servertype", ""),
                m.getString("serverhost", ""),
                m.getString("serverip", ""),
                m.getInt("serverport", 8085),
                m.getString("serverurl", ""),
                m.getLocalDateOrNow("lastpingdate"),
                m.getInt("isactive", 0),
                m.getLocalDateOrNow("lastpingdate"),
                m.getString("serverparams", "")
        );
    }
    @Override
    public java.lang.String toString() {
        return "AGENTSERVER,agentguid=" + agentGuid + ",serverguid=" + serverGuid + "servertype=" + serverType;
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "serverGuid";
    }

}
