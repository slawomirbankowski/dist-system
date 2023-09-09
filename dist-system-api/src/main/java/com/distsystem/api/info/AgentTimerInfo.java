package com.distsystem.api.info;

import java.util.List;

/** information class to keep timer attributes
 * Timers with tasks are defined in Dist services to perform repetitive work like cleaning cache
 * */
public class AgentTimerInfo {
    private String timerClassName;
    private long timerRunSeq;
    private int timerTasksCount;
    private List<AgentTimerTaskInfo> tasks;

    public AgentTimerInfo(String timerClassName, long timerRunSeq, int timerTasksCount, List<AgentTimerTaskInfo> tasks) {
        this.timerClassName = timerClassName;
        this.timerRunSeq = timerRunSeq;
        this.timerTasksCount = timerTasksCount;
        this.tasks = tasks;
    }

    public String getTimerClassName() {
        return timerClassName;
    }
    public long getTimerRunSeq() {
        return timerRunSeq;
    }
    public int getTimerTasksCount() {
        return timerTasksCount;
    }

    public List<AgentTimerTaskInfo> getTasks() {
        return tasks;
    }
}
