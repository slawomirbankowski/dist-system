package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentObjects;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;

import java.util.*;
import java.util.stream.Collectors;

/** service with shared objects in distributed environment */
public class AgentObjectsImpl extends ServiceBase implements AgentObjects {

    /** all in-memory objects */
    private final HashMap<String, DistMemoryObject> objects = new HashMap<>();

    /** creates new Objects service for shared objects */
    public AgentObjectsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.objects;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Objects to be kept in local Agent only.";
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("objects-count", (m, req) -> req.responseOkText(""+objects.size()))
                .addHandlerGet("objects-keys", (m, req) -> req.responseOkJsonSerialize(getObjectKeys()))
                .addHandlerGet("objects-keys-starts", (m, req) -> req.responseOkJsonSerialize(getObjectKeysStarts(req.getParamOne())))
                .addHandlerGet("object", (m, req) -> req.responseOkJsonSerializeOrNotFound(getObjectByKey(req.getParamOne())))
                .addHandlerGet("objects-search", (m, req) -> req.responseOkJsonSerialize(searchObjects(req.getParamOne())))
                .addHandlerGet("object-value", (m, req) -> req.responseOkTextOrNotFound(getObjectValueByKey(req.getParamOne())))
                .addHandlerPost("object", (m, req) -> req.responseOkJsonSerialize(setObjectValue(req.getParamOne(), req.getContentAsString())))
                .addHandlerPost("objects", (m, req) -> req.responseOkJsonSerialize(setObjects(req.getContentAsString())))
                .addHandlerDelete("object", (m, req) -> req.responseOkJsonSerialize(deleteObjectByKey(req.getParamOne())));
    }

    /** get all keys of objects */
    public Set<String> getObjectKeys() {
        return objects.keySet();
    }
    public Set<String> getObjectKeysStarts(String startWith) {
        return objects.keySet().stream().filter(key -> key.startsWith(startWith)).collect(Collectors.toSet());
    }
    /** get full object with metadata info by key */
    public Optional<DistMemoryObject> getObjectByKey(String key) {
        DistMemoryObject obj = objects.get(key);
        if (obj == null) {
            return Optional.empty();
        } else {
            obj.use();
            return Optional.of(obj);
        }
    }
    /** search objects by value */
    public List<DistMemoryObject> searchObjects(String str) {
        return objects.values().stream().filter(o -> o.getValue().contains(str)).toList();
    }
    /** get value by key */
    public Optional<String> getObjectValueByKey(String key) {
        return getObjectByKey(key).stream().map(DistMemoryObject::getValue).findFirst();
    }
    /** set value by key */
    public synchronized Map<String, Object> setObjectValue(String key, String value) {
        createEvent("setObjectValue");
        Optional<DistMemoryObject> currentObj = getObjectByKey(key);
        if (currentObj.isPresent()) {
            currentObj.get().setValue(value);
            return Map.of("exists", "true", "key", key, "object", currentObj.get());
        } else {
            DistMemoryObject newObj = new DistMemoryObject(value);
            objects.put(key, newObj);
            return Map.of("exists", "false", "key", key, "object", newObj);
        }
    }
    /** set values by keys */
    public synchronized List<Map<String, Object>> setObjects(String objectsJson) {
        createEvent("setObjects");
        Map<String, String> keyValues = JsonUtils.deserializeToMap(objectsJson);
        return keyValues.entrySet().stream().map(e -> {
            return setObjectValue(e.getKey(), e.getValue());
        }).toList();
    }
    /** delete object by key */
    public Map<String, Object> deleteObjectByKey(String key) {
        createEvent("deleteObjectByKey");
        DistMemoryObject obj = objects.remove(key);
        boolean exists = (obj!=null);
        return Map.of("exists", exists, "key", key);
    }


    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }

    /** change values in configuration bucket */
    public DistStatusMap initializeConfigBucket(DistConfigBucket bucket) {
        // INITIALIZE DAOs
        return DistStatusMap.create(this);
    }
    /** run after initialization */
    public void afterInitialization() {

    }
    @Override
    protected void onClose() {
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        return true;
    }
}
