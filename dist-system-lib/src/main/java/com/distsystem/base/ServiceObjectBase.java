package com.distsystem.base;

import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.interfaces.AgentComponent;

/** base class for service object */
public abstract class ServiceObjectBase extends AgentableBase implements AgentComponent {

    /** initial parameters for this object base */
    private ServiceObjectParams initialParams;

    /** creates new object for service with initialization params */
    public ServiceObjectBase(ServiceObjectParams params) {
        super(params.getAgent());
        this.initialParams = params;
    }
    /** get initial parameters for this service object */
    public ServiceObjectParams getInitialParams() {
        return initialParams;
    }

    /** get configuration property for this object by name */
    public String getConfigProperty(String name, String defaultValue) {
        return initialParams.getBucket().getProperty(name, defaultValue);
    }
    /** get configuration property for this object by name and cast it to Integer value */
    public int getConfigPropertyAsInt(String name, int defaultValue) {
        return initialParams.getBucket().getPropertyAsInt(name, defaultValue);
    }
    /** get configuration property for this object by name and cast it to Long value */
    public long getConfigPropertyAsLong(String name, long defaultValue) {
        return initialParams.getBucket().getPropertyAsLong(name, defaultValue);
    }
    /** get configuration property for this object by name and cast it to Double value */
    public double getConfigPropertyAsDouble(String name, double defaultValue) {
        return initialParams.getBucket().getPropertyAsDouble(name, defaultValue);
    }
    /** get configuration property for this object by name and cast it to Boolean value */
    public boolean getConfigPropertyAsBoolean(String name, boolean defaultValue) {
        return initialParams.getBucket().getPropertyAsBoolean(name, defaultValue);
    }

    @Override
    public boolean componentReinitialize() {
        return true;
    }
    @Override
    public DistComponentType getComponentType() {
        return DistComponentType.unknown;
    }
}
