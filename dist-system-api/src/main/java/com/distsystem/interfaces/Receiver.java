package com.distsystem.interfaces;

import com.distsystem.api.*;
import java.util.function.Function;

/** basic interface for service in distributed environment
 * service is a module or class that is cooperating with agent, could be registered
 * */
public interface Receiver extends DistService {

    /** register new method to process received messages */
    void registerReceiverMethod(String method, Function<DistMessage, DistMessage> methodToProcess);
    /** get number of receiver methods */
    int getMethodsCount();

}
