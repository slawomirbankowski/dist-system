package com.distsystem.interfaces;

import com.distsystem.api.info.AgentConnectorsInfo;
import com.distsystem.api.DistMessage;
import com.distsystem.api.DistMessageFull;
import com.distsystem.base.dtos.DistAgentServerRow;

import java.util.List;

/** interface for agent connectors manager
 * this is to keep connections to other agents */
public interface AgentConnectors extends DistService {

    /** open servers for communication  */
    void openServers();
    /** check list of active servers and connect to the server if this is still not connected */
    void checkActiveServers(List<DistAgentServerRow> activeServers);
    /** get full information about connectors - servers, clients*/
    AgentConnectorsInfo getInfo();
    /** get count of servers */
    int getServersCount();
    /** get all UIDs of servers */
    List<String> getServerKeys();
    /** get number of clients */
    int getClientsCount();
    /** get client keys */
    List<String> getClientKeys();
    /** register new client created local as part of server */
    void registerLocalClient(AgentClient client);
    /** message send to agents, directed to services, selected method */
    void sendMessage(DistMessage msg);
    /** message send to agents, directed to services, selected method, add callbacks to be called when response would be back */
    void sendMessage(DistMessageFull msgCallbacks);
    /** mark response for this message, it is executing callbacks onResponse */
    void markResponse(DistMessage msg);

}
