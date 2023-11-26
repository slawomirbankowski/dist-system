package com.distsystem.interfaces;

import com.distsystem.api.info.AgentRegistrationInfo;
import com.distsystem.api.DistIssue;
import com.distsystem.api.info.AgentRegistrationsInfo;
import com.distsystem.api.dtos.DistAgentRegisterRow;
import com.distsystem.api.dtos.DistAgentServerRow;

import java.util.List;

/** basic interface for registration manager
 * each agent will register to registration services so other agents can get it from registration service
 * registration services could be build based on different central technology like:
 * JDBC compliant database, Elasticsearch, Kafka, Redis, ...
 *  */
public interface AgentRegistrations extends DistService {

    /** register this server */
    void registerServer(DistAgentServerRow servDto);

    /** get number of registration */
    int getRegistrationsCount();
    /** get UIDs for registration services */
    List<String> getRegistrationKeys();
    /** get information object about all registration */
    AgentRegistrationsInfo getInfo();
    /** get information infos about registration objects */
    List<AgentRegistrationInfo> getRegistrationInfos();

    /** get all servers from registration services */
    List<DistAgentServerRow> getServers();

    /** get number of known agents */
    int getAgentsCount();
    /** get list of agents possible to connect from this agent
     * this is list of all known agents read from registration services of from other agents */
    List<DistAgentRegisterRow> getAgents();
    /** add issue to registrations */
    void addIssue(DistIssue issue);

}
