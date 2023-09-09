package com.distsystem.interfaces;

import com.distsystem.api.info.AgentTimerInfo;

import java.util.Timer;
import java.util.function.Function;

/** interface for timers manager in agent
 * object to manager timers with scheduled tasks */
public interface AgentTimers {

    /** start timer */
    void setUpTimer(String timerName, String delayConfigName, long defaultTimerValue, Function<String, Boolean> onTask);
    /** start timer */
    void setUpTimer(String timerName, long delayMs, long periodMs, Function<String, Boolean> onTask);
    /** get number of timer tasks */
    int getTimerTasksCount();
    /** get information about timer and timer tasks */
    AgentTimerInfo getInfo();
    /** get timer associated with this timer manager */
    Timer getTimer();
    /** close  */
    void close();

}
