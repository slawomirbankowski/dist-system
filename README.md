<h2>Distributed Agent System</h2>
Distributed Agent System is Framework to easily create Cloud-compliant software. <br>
Agents help to create effective communication, keep common storages and distributed reports, organize DAOs, gathering events and issues, provide management of timers and threads.
<br/>
These Agents could be created in many places, many applications and have different services turned on.<br/>
At first - Agent is connecting to Registration Services (JDBC, Elasticsearch, Kafka, ...) and register itself, servers and services.<br/>
Then, Agent is opening Servers for communication with other clients.<br/>
Next, Agent is opening Web API for custom connections.<br/>
The last - Agent is checking other Agent connected to Registration Services and<br/>
<br/>
Current agent is running on localhost.<br/>

Each Agent has:
- Api - Web REST API to connect to Agent, typically on port 9999, using that API is is easy to check status of Agent, re-configure, change state and schedule some work;
- Authentication and Authorization - to keep users, roles, permission;
- Cache - distributed cache to keep objects in memory for fast access;
- Configuration - distributed common configuration of all services and components;
- Connectors - clients and servers to connect to other agents;
- Dao - manager for Data Access Object connections;
- Events - events from Agent system - it could be anything to let know that something just happened in service or component;
- Flow - to set-up streamming flow to transfer data;
- Issues - issues like errors and exceptions from Agent and dependent services;
- Machine Learning - manage of ML models;
- Measure - counters for measures over time; 
- Memory - monitoring of JVM memory users by agent;
- Monitor - custom monitors of hosts, IPs, ssh sessions, disks, memory, ...;
- Notification - sending emails, text messages, ping rest endpoints;
- Objects - any objects that live only on this Agent;
- Receiver - receiver of communication from other Agents;
- Registrations - global repository of agents, servers, clients, issues, events, configurations;
- Reports - global reports that can be executed on agents; 
- Schedule - distributed schedules for some actions;
- Semaphores - distributed semaphores to be blocked/reserved;
- Services - all services like Cache, Reports, Storages, Spaces, ...
- Space - common space with shared objects; 
- Storages - list of storages to keep data (JDBC, Elasticsearch, MongoDB, Cassandra) and connections to these storages
- Serializer -  serializer to serialize and deserialize messages and other objects in Agent
- Threads - management of threads used by Agent; agent.getAgentThreads()
- Timers - management of timers used by Agent; agent.getAgentTimers()
- Tags - set of custom String values to classify agent; agent.getAgentTags() 

### Registrations
Agent is registering to Registration service that can be JDBC-compliant database, Kafka, Redis, Elasticsearch, MongoDB, Cassandra or centralized Agent. There could be many registration storages that one agent is connected to.


### Services
Agent might be having many built-in services as well as user-defined services. 
${GET:/agent/services}

### API
Agent is having HTTP API Web interface to communicate. Please check Postman collections. 
List of API endpoints:


### Cache
Fully distributed cache to keep 
Each cache is creating agent that can communicate to other configured agents to provide fast read and distributed write.
Cache can be fully configured how to keep objects:
- time-based - it means that object is released/disposed after some time
- priority-based - it means that each object is disposed based on priority, top priority would be disposed last
- out-of-memory - large object would be disposed first, small would be disposed the last
- LRU-based - last recently used would not be disposed
- usage-factor-based - usage factor over time is calculated for each object
- keep with replace - replace will be only if new object is in cache, always there must be at least one object
  Cache is distributed and can be deployed as many instances with additional configuration.
- there is agent-based system to keep cache-instances connected
- there are callback to be set when something important is happening
- last usage date is available
- get cache size per storage
- refresh all caches with refresh mode

Cache can be connected to different storages to keep cache items and communicate between cache-agents:
- Redis
- Elasticseach
- Kafka
- JDBC-compliant database(s) (with DDL option)
- custom HTTP storage

Cache could be used as:
- library to be linked and used inside application
- standalone application having public API to be used and REST API
- code to be included "as it is"


<h2>TODO list</h2>
<ul>
    <li>Review DTO objects to be saved in DB</li>
    <li>Memory stats save and store</li>
    <li>DistFactory </li>
    <li>saving DAO to DB too many times</li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
    <li></li>
</ul>






