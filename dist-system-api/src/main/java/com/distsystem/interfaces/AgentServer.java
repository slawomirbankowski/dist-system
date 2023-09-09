package com.distsystem.interfaces;

import com.distsystem.api.info.AgentServerInfo;
import com.distsystem.api.DistConfig;

import java.time.LocalDateTime;

/** Interface for communication server between agent.
 * This could be as Socket server, UDP server, REST server, or anything for direct communication.
 * Direct communication helps to communicate fast without delay.
 * This is one-way communication (asynchronous) because response could be sent through different connector-server.
 * */
public interface AgentServer {
    /** get unique ID of this server */
    String getServerGuid();
    /** get configuration for this agent */
    DistConfig getConfig();
    /** returns true if agent has been already closed */
    boolean isClosed();
    /** get date and time of creating this agent */
    LocalDateTime getCreateDate();
    /** close all items in this agent */
    void close();
    /** get information about this server */
    AgentServerInfo getInfo();
}
