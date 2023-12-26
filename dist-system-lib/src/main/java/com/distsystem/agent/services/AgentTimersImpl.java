package com.distsystem.agent.services;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.DistConfig;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentTimerInfo;
import com.distsystem.api.info.AgentTimerTaskInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.AgentTimers;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/** implementation of timer manager with scheduled tasks */
public class AgentTimersImpl extends ServiceBase implements AgentTimers {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentTimersImpl.class);
    /** timer to schedule important check methods */
    private final Timer timer = new Timer();
    /** all registered tasks for timer */
    private final java.util.concurrent.ConcurrentLinkedQueue<AgentTimerTask> timerTasks = new java.util.concurrent.ConcurrentLinkedQueue<>();

    /** creates service manager for agent with parent agent assigned */
    public AgentTimersImpl(AgentInstance parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.timers;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "";
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("timers", (m, req) -> req.responseOkJsonSerialize(timerTasks.stream().map(t -> t.toInfo()).toList()))
                .addHandlerGet("timer-names", (m, req) -> req.responseOkJsonSerialize(timerTasks.stream().map(t -> t.getName()).toList()))
                .addHandlerPost("cancel", (m, req) -> req.responseOkJsonSerialize(cancelTimerWithInfo(req.getParamOne())))
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()));
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        initialized = true;
        return true;
    }

    /** get number of timer tasks */
    public int getTimerTasksCount() {
        return timerTasks.size();
    }
    /** get timer associated with this timer manager */
    public Timer getTimer() {
        return timer;
    }

    /** schedule timer */
    public void setUpTimer(String timerName, String delayConfigName, long defaultTimerValue, Function<String, Boolean> onTask) {
        long timerPeriod = getAgent().getConfig().getPropertyAsLong(delayConfigName, defaultTimerValue);
        touch("setUpTimer");
        setUpTimer(timerName, timerPeriod, timerPeriod, onTask);
    }
    /** set-up timer for given method */
    public void setUpTimer(String timerName, long delayMs, long periodMs, Function<String, Boolean> onTask) {
        touch("setUpTimer");
        createEvent("setUpTimer");
        log.info("Scheduling timer task for agent: " + parentAgent.getAgentGuid() + ", name: " + timerName + ", current tasks count: " + timerTasks.size() + ", delay: " + delayMs + ", period: " + periodMs);
        AgentTimerTask agentTask = new AgentTimerTask(timerName, delayMs, periodMs, onTask);
        TimerTask taskToBeScheduled = new TimerTask() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    checkCount.incrementAndGet();
                    boolean taskRet = onTask.apply("");
                    if (taskRet) {
                        agentTask.onSuccess(System.currentTimeMillis()- startTime);
                    } else {
                        agentTask.onError();
                    }
                } catch (Exception ex) {
                    // TODO: mark exception
                    agentTask.onException();
                    addIssueToAgent("setUpTimer", ex);
                    log.warn("Exception while executing task, reason: " + ex.getMessage());
                }
            }
        };
        agentTask.setTimerTask(taskToBeScheduled);
        timerTasks.add(agentTask);
        createdCount.incrementAndGet();
        timer.scheduleAtFixedRate(taskToBeScheduled, delayMs, periodMs);
    }
    /** cancel timer with task for given name */
    public boolean cancelTimer(String timerName) {
        touch("cancelTimer");
        createEvent("cancelTimer");
        timerTasks.stream().filter(tt -> tt.getName().equals(timerName)).forEach(tt -> {
            tt.close();
        });
        return true;
    }
    public AgentTimerInfo cancelTimerWithInfo(String timerName) {
        cancelTimer(timerName);
        return getInfo();
    }
    /** get list of infos for timer tasks */
    public List<AgentTimerTaskInfo> getInfoTasks() {
        return timerTasks.stream().map(t -> t.toInfo()).collect(Collectors.toList());
    }
    /** get information about timer and timer tasks */
    public AgentTimerInfo getInfo() {
        // String timerClassName, long timerRunSeq, int timerTasksCount
        return new AgentTimerInfo(timer.getClass().getName(), checkCount.get(), timerTasks.size(), getInfoTasks());
    }

    /** close all tasks and timer */
    protected void onClose() {
        log.info("Closing all tasks for agent: " + getParentAgentGuid() + ", tasks: " + timerTasks.size());
        timerTasks.stream().forEach(tt -> tt.close());
        log.info("Closing timer");
        timer.cancel();
    }

}

/** enriched class keeping timer task with name and statistics */
class AgentTimerTask {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentTimerTask.class);
    private LocalDateTime createdDate = LocalDateTime.now();
    private String name;
    private TimerTask task;
    private long delayMs;
    private long periodMs;
    private final AtomicLong timerRunSeq = new AtomicLong();
    private final AtomicLong errorsCount = new AtomicLong();
    private final AtomicLong exceptionsCount = new AtomicLong();
    private final AtomicLong totalRunTimeMs = new AtomicLong();

    /** method to be executed on time */
    private Function<String, Boolean> onTask;

    public AgentTimerTask(String name, long delayMs, long periodMs, Function<String, Boolean> onTask) {
        this.name = name;
        this.delayMs = delayMs;
        this.periodMs = periodMs;
        this.onTask = onTask;
    }
    public void setTimerTask(TimerTask task) {
        this.task = task;
    }
    public String getCreatedDate() {
        return createdDate.toString();
    }
    public String getName() {
        return name;
    }
    public TimerTask getTask() {
        return task;
    }
    public long getDelayMs() {
        return delayMs;
    }
    public long getPeriodMs() {
        return periodMs;
    }
    public long getTimerRunSeq() {
        return timerRunSeq.get();
    }
    public long getErrorsCount() {
        return errorsCount.get();
    }
    public long getAverageRunTimeMs() {
        if (timerRunSeq.get() > 0) {
            return totalRunTimeMs.get() / timerRunSeq.get();
        } else {
            return 0;
        }
    }
    public void onSuccess(long runTimeMs) {
        timerRunSeq.incrementAndGet();
        totalRunTimeMs.addAndGet(runTimeMs);
    }
    public void onError() {
        timerRunSeq.incrementAndGet();
        errorsCount.incrementAndGet();
    }
    public void onException() {
        timerRunSeq.incrementAndGet();
        exceptionsCount.incrementAndGet();
    }
    public AgentTimerTaskInfo toInfo() {
        return new AgentTimerTaskInfo(createdDate, name, delayMs, periodMs, timerRunSeq.get(), errorsCount.get(), exceptionsCount.get());
    }
    public void close() {
        try {
            task.cancel();
        } catch (Exception ex) {
            log.warn("Cannot cancel task for name: " + name + ", reason: " + ex.getMessage(), ex);
        }
    }
}
