package com.distsystem.interfaces;

import com.distsystem.api.DistConfigBucket;
import com.distsystem.utils.AdvancedMap;

import java.util.List;
import java.util.Optional;

public interface AgentMonitor extends DistService {

    /** create monitor object from Map, returns status map */
    AdvancedMap createMonitor(AdvancedMap monitorParams);
    /** create monitor object from bucket, returns status map */
    AdvancedMap createMonitor(DistConfigBucket bucket);
    /** get names of monitors */
    List<String> getMonitorNames();
    /** get GUIDs of monitors */
    List<String> getMonitorGuids();
    /** get monitor by name */
    Optional<AgentMonitorObject> getMonitorByName(String monitorName);
    /** get monitor by GUID */
    Optional<AgentMonitorObject> getMonitorByGuid(String guid);

}
