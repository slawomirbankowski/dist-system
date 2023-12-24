package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentIssues;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

/** Implementation of Issues service to track all exceptions inside agent.
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
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 1L + issues.size()*6L;
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.issues;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Keep all issues like errors, exceptions from Agent.";
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("issues", (m, req) -> req.responseOkJsonSerialize(issues.stream().map(e -> e.toRow()).toList()))
                .addHandlerPost("clear", (m, req) -> req.responseOkJsonSerialize(clearIssues()))
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()));
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        return true;
    }
    /** add issue to be revoked by parent
     * issue could be Exception, Error, problem with connecting to storage,
     * internal error, not consistent state that is unknown and could be used by parent manager */
    public void addIssue(DistIssue issue) {
        synchronized (issues) {
            issues.add(issue);
            // add issue for registration services
            parentAgent.getRegistrations().addIssue(issue);
            while (issues.size() > parentAgent.getConfig().getPropertyAsLong(DistConfig.AGENT_CACHE_ISSUES_MAX_COUNT, DistConfig.AGENT_CACHE_ISSUES_MAX_COUNT_VALUE)) {
                issues.poll();
            }
        }
    }
    /** add issue with method and exception */
    public void addIssue(String methodName, Exception ex) {
        addIssue(new DistIssue(this, methodName, ex));
    }
    /** clear all issues */
    public String clearIssues() {
        createEvent("clearIssues");
        synchronized (issues) {
            issues.clear();
        }
        return "";
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
