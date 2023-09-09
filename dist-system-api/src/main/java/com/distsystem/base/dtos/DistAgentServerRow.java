package com.distsystem.base.dtos;

import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** row for JDBC table distagentregister
 * create table distagentserver(agentguid text, servertype text, serverhost text, serverip text, serverport int, serverurl text, createddate timestamp, isactive int, lastpingdate timestamp)
 * */
public class DistAgentServerRow {

    public String agentguid;
    public String serverguid;
    public String servertype;
    public String serverhost;
    public String serverip;
    public int serverport;
    public String serverurl;
    public LocalDateTime createddate;
    public int isactive;
    public LocalDateTime lastpingdate;
    public String serverparams;

    /** */
    public DistAgentServerRow() {
    }
    public DistAgentServerRow(String agentguid, String serverguid, String servertype, String serverhost, String serverip, int serverport, String serverurl,
                              LocalDateTime createddate, int isactive, LocalDateTime lastpingdate, String serverparams) {
        this.agentguid = agentguid;
        this.serverguid = serverguid;
        this.servertype = servertype;
        this.serverhost = serverhost;
        this.serverip = serverip;
        this.serverport = serverport;
        this.serverurl = serverurl;
        this.createddate = createddate;
        this.isactive = isactive;
        this.lastpingdate = lastpingdate;
        this.serverparams = serverparams;
    }
    public DistAgentServerRow(String agentguid) {
        this.agentguid = agentguid;
        this.serverguid = "";
        this.servertype = "";
        this.serverhost = "";
        this.serverip = "";
        this.serverport = 0;
        this.serverurl = "";
        this.createddate = LocalDateTime.now();
        this.isactive = 1;
        this.lastpingdate = LocalDateTime.now();
        this.serverparams = "";
    }
    public void deactivate() {
        this.isactive = 0;
    }
    public DistAgentServerRow copyNoPassword() {
        return new DistAgentServerRow(agentguid, serverguid, servertype, serverhost, serverip, serverport, serverurl, createddate, isactive, lastpingdate, serverparams);
    }
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.putAll(Map.of("type", "server",
                "agentguid", agentguid,
                "serverguid", serverguid,
                "servertype", servertype,
                "serverhost", serverhost,
                "serverip", serverip,
                "serverport", "" + serverport,
                "serverurl", serverurl,
                "createddate", createddate.toString(),
                "lastpingdate", lastpingdate.toString()));
        map.putAll(Map.of("isactive", ""+isactive,
                "serverparams", serverparams));
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
        return "AGENTSERVER,agentguid=" + agentguid + ",serverguid=" + serverguid + "servertype=" + servertype;
    }
}
