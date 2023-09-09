package com.distsystem.base.dtos;

import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/** row for distagentconfig table
 * create table distagentconfig(agentguid varchar(300), configname varchar(300), configvalue varchar(300), createddate timestamp, lastupdateddate timestamp)
 *
 * */
public class DistAgentConfigRow {

    public final String agentguid;
    public final String configname;
    public String configvalue;
    public LocalDateTime createddate;
    public LocalDateTime lastupdateddate;

    public DistAgentConfigRow(String agentguid, String configname, String configvalue, LocalDateTime createddate, LocalDateTime lastupdateddate) {
        this.agentguid = agentguid;
        this.configname = configname;
        this.configvalue = configvalue;
        this.createddate = createddate;
        this.lastupdateddate = lastupdateddate;
    }

    public static DistAgentConfigRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentConfigRow(
                m.getStringOrEmpty("agentguid"),
                m.getStringOrEmpty("configname"),
                m.getStringOrEmpty("configvalue"),
                m.getLocalDateOrNow("createddate"),
                m.getLocalDateOrNow("lastupdateddate")
        );
    }
}
