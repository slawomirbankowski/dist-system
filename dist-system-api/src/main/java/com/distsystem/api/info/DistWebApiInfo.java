package com.distsystem.api.info;

public class DistWebApiInfo {

    private final long requestMethodHandlersCount;
    private final long messageHandlersCount;
    private final long receivedMessagesCount;
    private final long exceptionMessagesCount;
    private final long handledRequestsCount;
    private final long handledRequestsTimeMs;

    public DistWebApiInfo(long requestMethodHandlersCount, long messageHandlersCount,
                          long receivedMessagesCount, long exceptionMessagesCount,
                          long handledRequestsCount, long handledRequestsTimeMs) {
        this.requestMethodHandlersCount = requestMethodHandlersCount;
        this.messageHandlersCount = messageHandlersCount;
        this.receivedMessagesCount = receivedMessagesCount;
        this.exceptionMessagesCount = exceptionMessagesCount;
        this.handledRequestsCount = handledRequestsCount;
        this.handledRequestsTimeMs = handledRequestsTimeMs;
    }

    public long getRequestMethodHandlersCount() {
        return requestMethodHandlersCount;
    }

    public long getMessageHandlersCount() {
        return messageHandlersCount;
    }

    public long getReceivedMessagesCount() {
        return receivedMessagesCount;
    }

    public long getExceptionMessagesCount() {
        return exceptionMessagesCount;
    }

    public long getHandledRequestsCount() {
        return handledRequestsCount;
    }

    public long getHandledRequestsTimeMs() {
        return handledRequestsTimeMs;
    }
}
