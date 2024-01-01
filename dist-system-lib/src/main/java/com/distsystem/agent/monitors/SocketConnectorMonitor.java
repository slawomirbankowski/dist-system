package com.distsystem.agent.monitors;

import com.distsystem.api.AgentMonitorInput;
import com.distsystem.api.AgentMonitorOutput;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.base.MonitorBase;

/** monitor to */
public class SocketConnectorMonitor extends MonitorBase {

    /** creates new monitor */
    public SocketConnectorMonitor(ServiceObjectParams params) {
        super(params);
    }
    /** run monitor internally */
    public AgentMonitorOutput runInternal(AgentMonitorInput in) {

        return new AgentMonitorOutput();
    }
    /** count objects created inside this monitor */
    @Override
    protected long countObjectsAgentable() {
        return 1L;
    }
    /** close this monitor*/
    @Override
    protected void onClose() {
    }
}
