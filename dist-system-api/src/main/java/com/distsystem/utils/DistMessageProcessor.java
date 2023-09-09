package com.distsystem.utils;

import com.distsystem.api.DistMessage;
import com.distsystem.api.info.AgentMessageProcessorInfo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/** processor is helper class for message processing with selection of method to be executed
 * this could be used in service */
public class DistMessageProcessor {

    /** counter of received messages */
    private final AtomicLong receivedMessages = new AtomicLong();
    private final AtomicLong exceptionMessages = new AtomicLong();
    /** key -> method name
     * value - method to process this message */
    private HashMap<String, BiFunction<String, DistMessage, DistMessage>> methodProcessors = new HashMap<>();
    /** default processor for messages  */
    private BiFunction<String, DistMessage, DistMessage> defaultProcessor = this::defaultMethodReturnsNotSupported;

    /** creates new empty processor for incoming messages */
    public DistMessageProcessor() {
    }

    /** get information about message processor */
    public AgentMessageProcessorInfo getInfo() {
        return new AgentMessageProcessorInfo(methodProcessors.size(), receivedMessages.get(), exceptionMessages.get());
    }
    /** get number of received messages */
    public long getReceivedMessagesCount() {
        return receivedMessages.get();
    }
    /** get number of error messages */
    public long getErrorMessagesCount() {
        return exceptionMessages.get();
    }
    /** get number of registered methods to process messages */
    public int getMethodsCount() {
        return methodProcessors.size();
    }
    /** */
    public DistMessage defaultMethodReturnsNotSupported(String methodName, DistMessage msg) {
        return msg.notSupported();
    }
    /** add many methods */
    public DistMessageProcessor addMethods(Object service) {
        for (Method method : service.getClass().getDeclaredMethods()) {
            System.out.println(">>>> Method: " + method.getName() + ", return type: " + method.getReturnType().getName());
            boolean retTypeOk = method.getReturnType().isAssignableFrom(DistMessage.class);
            var paramsCount = method.getParameterCount();
            var paramFirstOk = (paramsCount>0)?method.getParameters()[0].getType().isAssignableFrom(DistMessage.class):false;
            if (retTypeOk && paramsCount == 1 && paramFirstOk) {
                addMethod(method.getName(), (x, y) -> {
                    try {
                        return (DistMessage) method.invoke(service, y);
                    } catch (Exception ex) {
                        return DistMessage.createError("Incorrect invoke of method: " + method.getName(), ex);
                    }
                });
            }
        }
        return this;
    }
    /** add method to processor for given method, each service might have many methods to be called */
    public DistMessageProcessor addMethod(String methodName, BiFunction<String, DistMessage, DistMessage> method) {
        methodProcessors.put(methodName, method);
        return this;
    }
    /** */
    public DistMessageProcessor setDefaultMethod(BiFunction<String, DistMessage, DistMessage> method) {
        defaultProcessor = method;
        return this;
    }
    /** process message - find method and execute it */
    public DistMessage process(String methodName, DistMessage msg) {
        receivedMessages.incrementAndGet();
        BiFunction<String, DistMessage, DistMessage> method = methodProcessors.get(methodName);
        try {
            if (method != null) {
                return method.apply(methodName, msg);
            } else {
                return defaultProcessor.apply(methodName, msg);
            }
        } catch (Exception ex) {
            // TODO: status is Exception

            exceptionMessages.incrementAndGet();
            return msg.exception(ex);
        }
    }
    /** close message processor */
    public void close() {

    }

}
