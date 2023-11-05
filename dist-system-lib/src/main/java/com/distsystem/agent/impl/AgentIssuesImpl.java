package com.distsystem.agent.impl;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentIssues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

/** Implementation of Issues Manager.
 * Issues could be added in case of incorrect data, Exception, any error or unsupported thing in service
 * Issues could be stored and analyzed.
 * */
public class AgentIssuesImpl extends ServiceBase implements AgentIssues {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentIssuesImpl.class);
    /** queue of issues reported when using cache */
    protected final Queue<DistIssue> issues = new LinkedList<>();
    /** */
    public AgentIssuesImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.issues;
    }
    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        return msg.notSupported();
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }
    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        return true;
    }
    /** add issue to be revoked by parent
     * issue could be Exception, Error, problem with connecting to storage,
     * internal error, not consistent state that is unknown and could be used by parent manager */
    public void addIssue(DistIssue issue) {
        synchronized (issues) {
            issues.add(issue);
            // add issue for registration services
            parentAgent.getAgentRegistrations().addIssue(issue);
            while (issues.size() > parentAgent.getConfig().getPropertyAsLong(DistConfig.AGENT_CACHE_ISSUES_MAX_COUNT, DistConfig.AGENT_CACHE_ISSUES_MAX_COUNT_VALUE)) {
                issues.poll();
            }
        }
    }

    /** add issue with method and exception */
    public void addIssue(String methodName, Exception ex) {
        addIssue(new DistIssue(this, methodName, ex));
    }

    /** get all recent issues with cache */
    public Queue<DistIssue> getIssues() {
        return issues;
    }

    /** close issues with clearing all */
    protected void onClose() {
        synchronized (issues) {
            log.info("Closing Issues, clearing all, count: " + issues.size());
            issues.clear();
        }
    }

}
