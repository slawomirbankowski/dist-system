package com.distsystem.interfaces;

import java.time.LocalDateTime;

/** */
public interface KafkaReceiver {

    /** get date and time of creation */
    LocalDateTime getCreateDate();
    String getTopicName();
    boolean isWorking();
    String getConsumerGroup();
    /** get total read */
    long getTotalRead();
    /** get total confirmed */
    long getTotalConfirmed();
    /** close this receiver - set working as false */
    void close();

}
