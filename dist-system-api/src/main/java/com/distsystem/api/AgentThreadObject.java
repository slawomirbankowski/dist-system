package com.distsystem.api;

import com.distsystem.api.info.AgentThreadInfo;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

/** Object to encapsulate Thread managed by Dist Agent */
public class AgentThreadObject {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentThreadObject.class);
    /** date and time of creation */
    private final LocalDateTime createdDate = LocalDateTime.now();
    /** global unique ID */
    private final String threadGuid = DistUtils.generateConnectorGuid(this.getClass().getSimpleName());
    private String threadFriendlyName;
    /** parent component of Agent system */
    private final AgentComponent parent;
    /** Thread created in Dist Agent system or Dist Service */
    private Thread thread;

    public AgentThreadObject(AgentComponent parent, Thread thread, String threadFriendlyName) {
        this.parent = parent;
        this.thread = thread;
        this.threadFriendlyName = threadFriendlyName;
    }
    /** extract information about this Thread */
    public AgentThreadInfo getInfo() {
        // LocalDateTime createdDate, String threadGuid, String threadName, String threadState, int threadPriority, long threadId
        return new AgentThreadInfo(createdDate, threadGuid, threadFriendlyName, thread.getName(), thread.getState().name(), thread.getPriority(), thread.getId(), thread.isAlive(), thread.getClass().getName());
    }
    public String getFriendlyName() {
        return threadFriendlyName;
    }
    public String getThreadGuid() {
        return threadGuid;
    }
    public String getThreadName() {
        return thread.getName();
    }
    public Map<String, Object> getInfoMap() {

        return Map.of("", "");
    }
    /** check if thread has guid or name or friendly name*/
    public boolean checkThread(String guidOrName) {
        return guidOrName.equals(threadGuid) || guidOrName.equals(threadFriendlyName) || guidOrName.equals(thread.getName());
    }
    /** stop this thread */
    public AgentThreadInfo stopThread() {

        return getInfo();
    }
    /** try to close stop current thread */
    public void close() {
        if (thread != null) {
            if (thread.isAlive()) {
                try {
                    thread.join(1000L);
                } catch (Exception ex) {
                    log.info("");
                }
            }
        }
    }

}
