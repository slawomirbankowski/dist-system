package com.distsystem.agent.registrations;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.*;
import com.distsystem.base.dtos.DistAgentIssueRow;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.base.RegistrationBase;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import com.distsystem.base.dtos.DistAgentServerRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** connector to Elasticsearch as agent manager - central point with registering/unregistering agents
 * */
public class RegistrationElasticsearch extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationElasticsearch.class);

    private String elasticUrl;
    private String elasticUser;
    private String elasticPass;
    private String registerIndexName;
    private String serverIndexName;
    private String serviceIndexName;
    private String issueIndexName;
    /** DAO to Elasticsearch */
    private DaoElasticsearchBase elasticDao;

    public RegistrationElasticsearch(AgentInstance parentAgent) {
        super(parentAgent);
    }
    /** run for initialization in classes */
    @Override
    protected void onInitialize() {
        elasticUrl = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_ELASTICSEARCH_URL, "");
        elasticUser = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_ELASTICSEARCH_USER, "");
        elasticPass = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_ELASTICSEARCH_PASS, "");
        registerIndexName = "distagentregister";
        serverIndexName = "distagentserver";
        issueIndexName = "distagentissue";
        serviceIndexName = "distagentservice";
        // parentAgent.getConfig().getPropertyAsLong()
        log.info("Initializing of Elasticsearch registration with URL: " + elasticUrl + ", agent: " + parentAgent.getAgentGuid());
        elasticDao = parentAgent.getAgentDao().getOrCreateDaoOrError(DaoElasticsearchBase.class, DaoParams.elasticsearchParams(elasticUrl, elasticUser, elasticPass));
        elasticDao.usedByComponent(this);
        // check connection to Elasticsearch, if needed - create index with default name
        log.info("Connected to Elasticsearch, url: " + elasticUrl + ", indices: " + elasticDao.getIndices().size() + ", agent: " + parentAgent.getAgentGuid());
        elasticDao.createIndicesWithCheck(Set.of(registerIndexName, serverIndexName, issueIndexName, serviceIndexName));
    }
    @Override
    protected boolean onIsConnected() {
        return elasticDao.getClusterInfo().size() > 0;
    }
    /** */
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        log.info("Registering Agent at Elasticsearch, URL: " + elasticUrl + ", index: " + registerIndexName);
        elasticDao.addOrUpdateDocument(registerIndexName, register.getAgentGuid(), register.toAgentRegisterRow().toMap());
        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    /** */
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {
        log.info("Unregistering Agent at Elasticsearch for GUID: " + register.getAgentGuid() + ", Elastic: " + elasticUrl);
        elasticDao.addOrUpdateDocument(registerIndexName, register.getAgentGuid(), register.toAgentRegisterRow().toMap());
        return new AgentConfirmation(register.getAgentGuid(), false, true, 0, List.of());
    }
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        log.info("Registration Elasticsearch ping agent for GUID: " + ping.getAgentGuid());
        Map<String, String> doc = ping.toMap();
        elasticDao.addOrUpdateDocument(registerIndexName, ping.getAgentGuid(), doc);
        return new AgentPingResponse(ping.getAgentGuid());
    }
    /** remove active agents without last ping date for more than X minutes */
    public boolean removeInactiveAgents(LocalDateTime beforeDate) {
        var agentsToInactivate = getAgents().stream().filter(a -> a.getIsactive() == 1 && a.getLastpingdate().isBefore(beforeDate)).collect(Collectors.toList());
        log.info("Deactivating old agent without ping before: " + beforeDate.toString() +", by agent:" + parentAgent.getAgentGuid() +", to deactivate: " + agentsToInactivate.size());
        agentsToInactivate.stream().forEach(a -> {
            log.info("Deactivating old agent without ping for GUID: " + a.getAgentguid() +", by agent: " + parentAgent.getAgentGuid() + ", lastPing: " + a.getLastpingdate().toString() + ", active: " + a.getIsactive());
            a.deactivate();
            elasticDao.addOrUpdateDocument(registerIndexName, a.getAgentguid(), a.toMap());
        });
        return true;
    }

    /** remove inactive agents with last ping date for more than X minutes */
    public boolean deleteInactiveAgents(LocalDateTime beforeDate) {
        var agentsDelete = getAgents().stream().filter(a -> a.getLastpingdate().isBefore(beforeDate)).collect(Collectors.toList());
        log.info("Deleting old agent without ping before: " + beforeDate.toString() +", by agent:" + parentAgent.getAgentGuid() + ", to delete: " + agentsDelete.size());
        agentsDelete.stream().forEach(a -> {
            log.info("Deleting old agent without ping for GUID: " + a.getAgentguid() +", by agent: " + parentAgent.getAgentGuid() + ", lastPing: " + a.getLastpingdate().toString());
            elasticDao.deleteDocument(registerIndexName, a.getAgentguid());
        });
        return true;
    }
    /** get normalized URL for this registration */
    public String getUrl() {
        return elasticUrl;
    }
    /** add issue for registration */
    public void addIssue(DistIssue issue) {
        DistAgentIssueRow row = issue.toRow();
        elasticDao.addOrUpdateDocument(issueIndexName, row.getGuid(), row.toMap());
    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
        elasticDao.addOrUpdateDocument(serverIndexName, serv.serverguid, serv.toMap());
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
        serv.deactivate();
        elasticDao.addOrUpdateDocument(serverIndexName, serv.serverguid, serv.toMap());
    }
    /** get all communication servers */
    public List<DistAgentServerRow> getServers() {
        return elasticDao.searchComplexToMaps(serverIndexName, "match", "type", "server", DistAgentServerRow::fromMap);
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {

        return false;
    }
    /** set active servers with last ping date before given date as inactive */
    public boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate) {

        return false;
    }
    /** register service */
    public void registerService(DistAgentServiceRow service) {
        log.info("Registering service in Elasticsearch, GUID: " + service.serviceguid + ", type: " + service.getServicetype());
        elasticDao.addOrUpdateDocument(serviceIndexName, service.getServiceguid(), service.toMap());
    }
    /** get agents from registration services */
    public List<DistAgentRegisterRow> getAgents() {
        return elasticDao.searchComplexToMaps(registerIndexName, "match", "type", "agent", DistAgentRegisterRow::fromMap);
    }
    /** close current connector */
    @Override
    protected void onClose() {
        try {
            log.warn("Closing Elasticsearch registration");
            elasticDao.close();
        } catch (Exception ex) {
        }
    }
}
