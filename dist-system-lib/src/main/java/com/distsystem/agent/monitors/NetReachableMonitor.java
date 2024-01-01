package com.distsystem.agent.monitors;

import com.distsystem.api.AgentMonitorInput;
import com.distsystem.api.AgentMonitorOutput;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.base.MonitorBase;

import java.net.InetAddress;

/** monitor to check if net address is reachable */
public class NetReachableMonitor extends MonitorBase {

    /** creates new monitor */
    public NetReachableMonitor(ServiceObjectParams params) {
        super(params);

    }
    /** run monitor internally */
    public AgentMonitorOutput runInternal(AgentMonitorInput in) throws Exception {

        InetAddress addr = java.net.InetAddress.getByName("");

        if (addr.isReachable(1000)) {

        } else {

        }
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
