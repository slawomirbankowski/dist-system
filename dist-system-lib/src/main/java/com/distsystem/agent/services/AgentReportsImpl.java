package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Service to keep  execute, delete, schedule, edit reports.
 * Each report is connected to Storage, has query to be executed, list of columns, filters, formatters, readers.
 * Report can be executed with parameters for filters, column subset, maximum rows, maximum execution/delivery time,
 * selected format, */
public class AgentReportsImpl extends ServiceBase implements AgentReports {

    /** local logger for this clas s*/
    protected static final Logger log = LoggerFactory.getLogger(AgentReportsImpl.class);

    public AgentReportsImpl(Agent agent) {
        super(agent);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.report;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "";
    }


    public List<String> getReportNames() {
        // TODO: get report names
        return parentAgent.getRegistrations().getRegistrationKeys();
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {

        // TODO: update configuration of this service
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        return true;
    }
    /** close and deinitialize service */
    public void onClose() {
        // TODO: close this reports service
    }

}
