package com.distsystem.api;

import com.distsystem.api.enums.DistCallbackType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** callbacks defined when response message would come back to calling agent */
public class DistCallbacks {

    /** */
    private boolean locked = false;
    /** all callbacks to be applied */
    private Map<String, Function<DistMessage, Boolean>> callbacks = new HashMap<>();

    /** */
    public DistCallbacks() {
    }

    /** add new callback - only if it is not locked */
    public DistCallbacks addCallback(DistCallbackType ct, Function<DistMessage, Boolean> callbackMethod) {
        if (!locked) {
            callbacks.put(ct.name(), callbackMethod);
        }
        return this;
    }
    /** lock callbacks to not add callbacks anymore */
    public DistCallbacks lock() {
        locked = true;
        return this;
    }
    /** apply callback to be run - if exists */
    public Boolean applyCallback(DistCallbackType ct, DistMessage msg) {
        Function<DistMessage, Boolean> cb = callbacks.get(ct.name());
        if (cb != null) {
            return cb.apply(msg);
        } else {
            return false;
        }
    }
    /** get number of callbacks to be called */
    public int getCallbacksCount() {
        return callbacks.size();
    }
    /** default method for no callback */
    public Boolean noCallback(DistMessage msg) {
        return true;
    }

    /** returns default empty callbacks */
    public final static DistCallbacks defaultCallbacks = new DistCallbacks().lock();
    /** create empty callbacks to add any custom callbacks */
    public final static DistCallbacks createEmpty() {
        return new DistCallbacks();
    }


}
