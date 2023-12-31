package com.distsystem.api.info;

public class AgentMessageProcessorInfo {

    private final int messageProcessorMethodsCount;
    private final long receivedMessagesCount;
    private final long exceptionMessagesCount;

    public AgentMessageProcessorInfo(int messageProcessorMethodsCount, long receivedMessagesCount, long exceptionMessagesCount) {
        this.messageProcessorMethodsCount = messageProcessorMethodsCount;
        this.receivedMessagesCount = receivedMessagesCount;
        this.exceptionMessagesCount = exceptionMessagesCount;
    }

    public int getMessageProcessorMethodsCount() {
        return messageProcessorMethodsCount;
    }

    public long getReceivedMessagesCount() {
        return receivedMessagesCount;
    }

    public long getExceptionMessagesCount() {
        return exceptionMessagesCount;
    }
}
