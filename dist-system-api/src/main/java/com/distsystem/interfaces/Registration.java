package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.api.info.AgentRegistrationInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Registration {
    /**
     * add issue for registration
     */
    void addIssue(DistIssue issue);

    /**
     * register server for communication
     */
    void addServer(DistAgentServerRow serv);

    /**
     * unregister server for communication
     */
    void unregisterServer(DistAgentServerRow serv);

    AgentPingResponse agentPing(AgentPing ping);

    AgentConfirmation agentUnregister(AgentRegister register);

    /**
     * inactivate active agents with last ping date for more than X minutes
     */
    boolean removeInactiveAgents(LocalDateTime beforeDate);

    /**
     * remove inactive agents with last ping date for more than X minutes
     */
    boolean deleteInactiveAgents(LocalDateTime beforeDate);

    /**
     * register service
     */
    void registerService(DistAgentServiceRow service);

    /**
     * get normalized URL for this registration
     */
    String getUrl();

    Map<String, Object> getRegistrationCustomParameters();

    AgentRegistrationInfo getInfo();

    /**
     * get all agents
     */
    List<DistAgentRegisterRow> getAgents();

    List<DistAgentRegisterRow> getAgentsActive();

    /**
     * get all communication servers
     */
    List<DistAgentServerRow> getServers();

    /**
     * get all shared storages
     */
    List<DistAgentStorageRow> getStorages();

    /**
     * get all shared storage names
     */
    List<String> getStorageNames();

    /**
     * get storage by name
     */
    Optional<DistAgentStorageRow> getStorageByName(String storageName);

    /**
     * add storage
     */
    boolean addStorage(DistAgentStorageRow storage);

    /**
     * get all shared reports
     */
    List<DistAgentReportRow> getReports();

    /**
     * get all shared report names
     */
    List<String> getReportNames();

    /**
     * get report by name
     */
    Optional<DistAgentReportRow> getReportByName(String reportName);

    /**
     * add report
     */
    boolean addReport(DistAgentReportRow report);

    /**
     * add report run
     */
    boolean addReportRun(DistAgentReportRunRow reportRun);

    /**
     * get all monitors
     */
    List<DistAgentMonitorRow> getMonitors();

    /**
     * get all monitor names
     */
    List<String> getMonitorNames();

    /**
     * get monitor by name
     */
    Optional<DistAgentMonitorRow> getMonitorByName(String reportName);

    /**
     * add monitor
     */
    boolean addMonitor(DistAgentMonitorRow monitor);

    /**
     * add monitor check
     */
    boolean addMonitorCheck(DistAgentMonitorCheckRow monitorCheck);

    /**
     * get all notifications
     */
    List<DistAgentNotificationRow> getNotifications();

    /**
     * add notification
     */
    boolean addNotification(DistAgentNotificationRow notif);

    /**
     * get all schedules
     */
    List<DistAgentScheduleRow> getSchedules();

    /**
     * add schedule
     */
    boolean addSchedule(DistAgentScheduleRow schedule);

    /**
     * add schedule
     */
    boolean addScheduleExecution(DistAgentScheduleRow schedule);

    /**
     * get all spaces
     */
    List<DistAgentSpaceRow> getSpaces();

    /**
     * get all space names
     */
    List<String> getSpaceNames();

    /**
     * get space by name
     */
    Optional<DistAgentSpaceRow> getSpaceByName(String spaceName);

    /**
     * add space
     */
    boolean addSpace(DistAgentSpaceRow space);

    /**
     * remove space
     */
    boolean removeSpace(String spaceName);

    /**
     * get all measures
     */
    List<DistAgentMeasureRow> getMeasures();

    /**
     * get all measure names
     */
    List<String> getMeasureNames();

    /**
     * add measure
     */
    boolean addMeasure(DistAgentMeasureRow measure);

    /**
     * get all queries
     */
    List<DistAgentQueryRow> getQueries();

    /**
     * get all query names
     */
    List<String> getQueryNames();

    /**
     * get query by name
     */
    Optional<DistAgentQueryRow> getQueryByName(String queryName);

    /**
     * add or edit query
     */
    boolean addQuery(DistAgentQueryRow measure);

    /**
     * add DAO row
     */
    boolean addDao(DistAgentDaoRow dao);

    /**
     * get all global resources for given type
     */
    List<DistAgentResourceRow> getResourcesForType(String resourceType);

    /**
     * get all global resource names for given type
     */
    List<String> getResourceNamesForType(String resourceType);

    /**
     * get resource by name
     */
    Optional<DistAgentResourceRow> getResourceByName(String queryName);

    /**
     * add new resources
     */
    boolean addResources(List<DistAgentResourceRow> resources);

    /**
     * get all settings
     */
    List<DistAgentSettingRow> getSettings();

    /**
     * search for settings
     */
    List<DistAgentSettingRow> searchSettings(String findStr);

    /**
     * add settings
     */
    boolean addSettings(List<DistAgentSettingRow> settings);

    /**
     * get resource by name
     */
    List<String> getScriptNames();

    /**
     * get script by name
     */
    Optional<DistAgentScriptRow> getScriptForName(String scriptName);

    /**
     * add script
     */
    boolean addScript(DistAgentScriptRow script);

    /**
     * ping given server by GUID
     */
    boolean serverPing(DistAgentServerRow serv);

    /**
     * set active servers with last ping date before given date as inactive
     */
    boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate);
}
