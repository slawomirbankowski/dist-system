package com.distsystem.api.enums;

/** all services that can be connected to Dist System, each service can have some features and methods to be used
 * There might be different distributed services like:
 * agent - communication between distributed processes
 * cache - keep objects in memory or other fast key-read storage to have faster access to these objects
 * space - shared object in global space of distributed agents
 * measure - global counters
 * flow - distributed flow
 * report - distributed shared reports to be executed
 * config - distributed configuration with access to many storages and versioned values over time
 * schedule - distributed orchestration like CRON in the Cloud
 * custom - any custom, unknown service, external from DistSystem point of view
 * */
public enum DistServiceType {
    agent, // agent service to bind all other services, providing communication, thread management, issues and events management
    api, // Web API for REST endpoints
    auth, // authentication and authorization
    cache, // distributed cache
    config, // configuration reader and
    connectors, // connectors - direct servers and clients
    daos, // DAOs to connect to Agent storages
    events, // events in current agent to subscribe to
    flow, // flow of rows in distributed environment
    issues, // issues in current agent
    measure, // measures in distributed environment
    memory, // register memory used by agent
    ml, // ML manager to create ML models
    monitor, // monitors to check external hosts, services, 
    notification,
    objects, // shared read-only distributed objects, cached and distributed like key-value store
    receiver, // receiver
    registrations, // agent registration to know agents around
    remote, // remote execution of methods on registred objects
    report, // executing reports based on storages
    semaphores, // service with distributed semaphores
    security, // distributed security: authentication AND authorization
    services, // service manager for all agent services
    schedule, // schedule for any tasks, scripts or flows
    space, // shared spaces with objects with owners but allowed to be modified by anyone
    storage, // distributed storages
    threads, // thread management
    timers, // timers used in Agent
    version,
    custom
}
