package com.distsystem.base;

import com.distsystem.agent.impl.Agentable;
import com.distsystem.api.DistServiceInfo;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.DistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** base class for any service connected to Agent */
public abstract class ServiceBase extends Agentable implements DistService {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(ServiceBase.class);
    /** UUID for service - globally unique */
    protected String guid = createServiceUid();
    /** if service has been already closed */
    protected boolean closed = false;
    /** list of components */
    private List<AgentComponent> componentList = new LinkedList<>();

    /** creates new service with agent */
    public ServiceBase(Agent parentAgent) {
        super(parentAgent);
    }

    /** get unique ID of this service */
    public String getServiceUid() {
        return guid;
    }
    /** create new service UID for this service */
    protected abstract String createServiceUid();
    /** get basic information about service */
    public DistServiceInfo getServiceInfo() {
        return new DistServiceInfo(getServiceType(), getClass().getName(), getServiceUid(), createDate, closed, getServiceInfoCustomMap());
    }
    /** get row for registration services */
    public DistAgentServiceRow getServiceRow() {
        // String agentguid, String serverguid, String servicetype, LocalDateTime createddate, int isactive, LocalDateTime lastpingdate
        return new DistAgentServiceRow(parentAgent.getAgentGuid(), "", getServiceType().name(), createDate, (closed)?0:1, LocalDateTime.now());
    }
    /** add component to this service */
    public void addComponent(AgentComponent component) {
        componentList.add(component);
    }
    /** get custom map of info about service */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of();
    }
    /** check if service has been already closed and deinitialized */
    public boolean getClosed() { return closed; }
    /** close all items in this service */
    protected abstract void onClose();
    /** close and deinitialize service - remove all items, disconnect from all storages, stop all timers */
    public final void close() {
        if (closed) {
            log.warn("Service is already closed for UID: " + getServiceUid());
        } else {
            closed = true;
            log.info("Closing cache for GUID: " + getServiceUid());
            onClose();
        }
    }

}
