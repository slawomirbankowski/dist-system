package com.distsystem.api.info;

import java.time.LocalDateTime;

/** information class for timer task
 * */
public class AgentTimerTaskInfo {

    private final LocalDateTime createdDate;
    private final String name;
    private final long delayMs;
    private final long periodMs;
    private final long timerRunSeq;
    private final long errorsCount;
    private final long exceptionsCount;

    public AgentTimerTaskInfo(LocalDateTime createdDate, String name, long delayMs, long periodMs, long timerRunSeq, long errorsCount, long exceptionsCount) {
        this.createdDate = createdDate;
        this.name = name;
        this.delayMs = delayMs;
        this.periodMs = periodMs;
        this.timerRunSeq = timerRunSeq;
        this.errorsCount = errorsCount;
        this.exceptionsCount = exceptionsCount;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public String getName() {
        return name;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public long getPeriodMs() {
        return periodMs;
    }

    public long getTimerRunSeq() {
        return timerRunSeq;
    }

    public long getErrorsCount() {
        return errorsCount;
    }

    public long getExceptionsCount() {
        return exceptionsCount;
    }
}
