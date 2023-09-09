package com.distsystem.api;

import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.interfaces.AgentClient;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** message with callbacks and registered client that was used to send this message, option to retry and some debug information about sending */
public class DistMessageFull {

    /** created date and time of this full message */
    private LocalDateTime createdDate = LocalDateTime.now();
    /** message to be sent */
    private final DistMessage message;
    /** callbacks to be called when response would be back or there will be timeout or error while sending or resending */
    private final DistCallbacks callbacks;
    /** list of client GUIDs that was used to send this message */
    private final List<String> clientGuids = new LinkedList<>();
    /** number of successful send actions */
    private AtomicInteger sentOkCount = new AtomicInteger();
    /** number of retries to send this message  */
    private AtomicInteger sendingRetry = new AtomicInteger(3);

    /** creates new full message */
    public DistMessageFull(DistMessage msg, DistCallbacks callbacks) {
        this.message = msg;
        this.callbacks = callbacks;
    }
    /** get message to be sent */
    public DistMessage getMessage() {
        return message;
    }
    /** get callbacks to be called for events */
    public DistCallbacks getCallbacks() {
        return callbacks;
    }

    /** get create date and time of this full message */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    /** get client GUIDs that were used to send this message with */
    public List<String> getClientGuids() {
        return clientGuids;
    }
    /** get number of successful sent actions */
    public int getSentOkCount() {
        return sentOkCount.get();
    }

    /** add client that sent this message */
    public void addClient(AgentClient client)  {
        clientGuids.add(client.getClientGuid());
    }
    /** increment sentOKCount-er */
    public void sendWithSuccess() {
        sentOkCount.incrementAndGet();
    }
    public void sendWithResult(boolean isOk) {
        if (isOk) {
            sendWithSuccess();
        }
    }
    /** apply callback - run method if exists */
    public Boolean applyCallback(DistCallbackType ct) {
        return callbacks.applyCallback(ct, message);
    }
    /** */
    public Boolean applyCallback(DistCallbackType ct, DistMessage responseMsg) {
        return callbacks.applyCallback(ct, responseMsg);
    }

}
