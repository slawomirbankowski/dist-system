package com.distsystem.interfaces;

import com.distsystem.api.info.AgentApisInfo;

import java.util.List;

/** interface for Web Api in Agent - this is technical API to get into given agent directly - synchronous communication
 * Through Agent Web API other services might communicate with Agent directly to given instance of an Agent
 * All services can register methods to be executed with that direct communication Request-Response
 *
 * */
public interface AgentApi {

    /** get parent agent connected to this API implementation */
    Agent getAgent();
    /** get first non empty Web API port */
    int getPort();
    /** open all known APIs for this agent */
    void openApis();
    /** get count of APIs */
    int getApisCount();
    /** get information structure about APIs */
    AgentApisInfo getInfo();
    /** get all UIDs of servers */
    List<String> getApiTypes();
    /** check all registered APIs */
    void checkApis();
    /** close this manager */
    void close();

}
