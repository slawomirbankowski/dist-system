package com.distsystem.base;

import com.distsystem.api.AgentMonitorInput;
import com.distsystem.api.AgentMonitorOutput;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.AgentMonitorObject;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;

/** base class for monitoring different things like servers, HTTP, IPs, databases, ... */
public abstract class MonitorBase extends ServiceObjectBase implements AgentMonitorObject {

    /** name of this monitor */
    private final String monitorName;
    private final String monitorType;

    /** creates base monitor */
    public MonitorBase(ServiceObjectParams params) {
        super(params);
        this.monitorType = params.getBucket().getKey().getConfigType();
        this.monitorName = params.getBucket().getKey().getConfigInstance();
        params.getBucket().getEntries();
    }

    /** get name of this monitor */
    public String getMonitorName() {
        return monitorName;
    }
    /** create unique agentable UID */
    protected String createGuid() {
        return DistUtils.generateCustomGuid("MONOBJ_" + parentAgent.getAgentShortGuid());
    }
    /** create status map from this monitor*/
    public AdvancedMap toStatusMap() {

        return AdvancedMap.createFromObject(this);
    }
    /** run this monitor */
    public void run() {
        try {
            AgentMonitorInput in = new AgentMonitorInput();
            var out = runInternal(in);

        } catch (Exception ex) {

        }
    }
    /** run given monitor */
    public abstract AgentMonitorOutput runInternal(AgentMonitorInput in) throws Exception;

}
