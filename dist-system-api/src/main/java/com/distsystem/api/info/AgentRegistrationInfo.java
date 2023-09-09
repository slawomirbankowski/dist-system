package com.distsystem.api.info;

import com.distsystem.api.AgentConfirmation;

import java.time.LocalDateTime;
import java.util.Map;

/** information about Registration class to register Agent */
public class AgentRegistrationInfo {
    private String registerGuid;
    private String registrationType;
    private LocalDateTime createdDate;
    private boolean initialized;
    private boolean closed;
    private boolean lastConnected;
    private String url;
    private AgentConfirmation confirmation;

    private Map<String, Object> parameters;

    public AgentRegistrationInfo(String registerGuid, String registrationType, LocalDateTime createdDate, boolean initialized, boolean closed, boolean lastConnected, String url,
                                 AgentConfirmation confirmation, Map<String, Object> parameters) {
        this.registerGuid = registerGuid;
        this.registrationType = registrationType;
        this.createdDate = createdDate;
        this.initialized = initialized;
        this.closed = closed;
        this.lastConnected = lastConnected;
        this.url = url;
        this.confirmation = confirmation;
        this.parameters = parameters;
    }

    public String getRegisterGuid() {
        return registerGuid;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isLastConnected() {
        return lastConnected;
    }

    public String getUrl() {
        return url;
    }

    public AgentConfirmation getConfirmation() {
        return confirmation;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
