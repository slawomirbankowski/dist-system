package com.distsystem.api;

import com.distsystem.base.AgentableBase;
import com.distsystem.interfaces.DistService;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** bucket of configuration values for given group and key */
public class DistConfigBucket extends AgentableBase {
    private static final Logger log = LoggerFactory.getLogger(DistConfigBucket.class);

    /** parent configuration group */
    private final DistConfigGroup parentGroup;
    /** bucket key */
    private final DistConfigBucketKey key;
    /** list of configuration entries */
    private final List<DistConfigEntry> entries;
    /** all entries serialized */
    private final String entriesSerialized;
    /** hash of all entries added to this bucket */
    private final String entriesHash;

    /** creates new configuration bucket */
    public DistConfigBucket(DistConfigGroup parentGroup, DistConfigBucketKey key, List<DistConfigEntry> entries) {
        super(parentGroup.getAgent());
        this.parentGroup = parentGroup;
        this.key = key;
        this.entries = entries;
        this.entries.sort(Comparator.comparing(DistConfigEntry::getFullConfig));
        this.entriesSerialized = this.entries.stream().map(x -> x.toString()).collect(Collectors.joining());
        this.entriesHash = DistUtils.fingerprint(entriesSerialized);
        log.info("Creating new DistConfigBucket for service: " + parentGroup.getParentService().getServiceType() + ", ");
    }

    @Override
    protected long countObjectsAgentable() {
        return 0;
    }
    /** create unique agentable UID */
    protected String createGuid() {
        return DistUtils.generateCustomGuid("CFGBCK_" + parentAgent.getAgentShortGuid());
    }
    @Override
    protected void onClose() {
    }

    /** */
    public AdvancedMap initializeConfigBucket() {
        log.info("Initialize new config bucket for service: " + key.getServiceName() + ", ConfigType: " + key.getConfigType() + ", instance: " + key.getConfigInstance() + ", entries: " + entries.size());
        parentGroup.getParentConfig().mergeWithEntries(entries);
        AdvancedMap result = parentGroup.getParentService().initializeConfigBucket(this);
        return result;
    }
    /** get key-values */
    public Map<String, String> getKeyValues() {
        HashMap<String, String> m = new HashMap<String, String>();
        entries.stream().forEach(e -> m.put(e.getConfigSetting(), e.getConfigValue()));
        return m;
    }
    /** convert property name like URL into full property name using group, config type and name
     * EXAMPLE: input=URL, output=
     * */
    private String convertPropertyName(String name) {
        if (name.startsWith("AGENT_")) {
            return name;
        } else {
            String propertyInstance = (key.getConfigInstance().equals(DistConfig.PRIMARY))?"":"_"+key.getConfigInstance();
            return parentGroup.getGroupName() + "_" + key.getConfigType() + "_" + name + propertyInstance;
        }
    }
    /** get property value for given name */
    public String getProperty(String name) {
        return parentGroup.getParentConfig().getProperty(convertPropertyName(name));
    }
    /** get property for this bucket */
    public String getProperty(String name, String defaultValue) {
        String fullConfig = convertPropertyName(name);
        log.info("Get config property for object, group: " + parentGroup.getGroupName() + ", name: " + name + ", fullConfig: " + fullConfig);
        return parentGroup.getParentConfig().getProperty(fullConfig, defaultValue);
    }
    /** get property for given name as Long value */
    public long getPropertyAsLong(String name, long defaultValue) {
        return DistUtils.parseLong(getProperty(name), defaultValue);
    }
    /** get property for given name as Int value */
    public int getPropertyAsInt(String name, int defaultValue) {
        return DistUtils.parseInt(getProperty(name), defaultValue);
    }
    /** get property for given name as Double value */
    public double getPropertyAsDouble(String name, double defaultValue) {
        return DistUtils.parseDouble(getProperty(name), defaultValue);
    }
    /** get property for given name as Boolean value */
    public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
        return DistUtils.parseBoolean(getProperty(name), defaultValue);
    }

    public DistConfigGroup getParentGroup() {
        return parentGroup;
    }
    public DistConfigBucketKey getKey() {
        return key;
    }
    /** get configuration entries from this bucket */
    public List<DistConfigEntry> getEntries() {
        return entries;
    }
    /** get all configuration entries serialized */
    public String getEntriesSerialized() {
        return entriesSerialized;
    }
    public String getEntriesHash() {
        return entriesHash;
    }
    /** creates new configuration bucket */
    public static DistConfigBucket createBucket(DistConfigGroup parentGroup, DistConfigBucketKey key, List<DistConfigEntry> entries) {
        return new DistConfigBucket(parentGroup, key, entries);
    }
    /** Creates new configuration bucket
     * configType -
     * AGENT_CONFIG_OBJECT_JDBC_URL_PRIMARY
     * AGENT_serviceName_serviceGroup_configType_configSetting_configInstance
     *  serviceName=AUTH, DAOS,
     *  serviceGroup=OBJECT, SERVER, ...
     *  configType=JDBC, KAFKA, ELASTICSEARCH, MONGODB, ...
     *  configSetting=URL, USER, BROKERS, PASS, ...
     *  configInstance=PRIMARY, SECONDARY, TETRIARY, ...
     *
     * */
    public static DistConfigBucket createBucket(DistService parentService, String serviceGroup, String configType, String configInstance, Map<String, String> configValues) {
        String srvName = parentService.getServiceType().name().toUpperCase();
        String groupName = "AGENT_" + srvName + "_" + serviceGroup; // DistConfig.AGENT_AUTH_STORAGE, AGENT_CONFIGREADER_OBJECT, AGENT_REGISTRATION_OBJECT, ...
        DistConfigGroup group = new DistConfigGroup(parentService.getConfig(), groupName, parentService);
        DistConfigBucketKey bucketKey = new DistConfigBucketKey(srvName, configType, configInstance);
        List<DistConfigEntry> configEntries = configValues.entrySet().stream().map(cv -> {
            String configFullKey = "AGENT_" + srvName + "_" + serviceGroup + "_" + configType + "_" + cv.getKey() + "_" + configInstance;
            String configFullValue = cv.getValue();
            log.debug("----> Add new configuration value, fullKey: " + configFullKey);
            DistConfigEntry entry = new DistConfigEntry(groupName, configFullKey, srvName, serviceGroup, configType, cv.getKey(), configInstance, configFullValue);
            return entry;
        }).collect(Collectors.toList());
        return createBucket(group, bucketKey, configEntries);
    }

    /** Creates new configuration bucket from parameters and service. Minimum parameters that are needed:
     * serviceGroup - OBJECT, SERVER, ...
     * configType - JDBC, KAFKA, ELASTICSEARCH, MONGODB, ...
     * configInstance - PRIMARY, SECONDARY, TETRIARY, ...
     * */
    public static DistConfigBucket createBucket(DistService parentService, AdvancedMap bucketParams) {
        String serviceGroup = bucketParams.getString("serviceGroup", "OBJECT");
        String configType = bucketParams.getString("configType", "NONE");
        String configInstance = bucketParams.getString("configInstance", "PRIMARY");
        Map<String, String> configValues = bucketParams.getMap();
        return createBucket(parentService, serviceGroup, configType, configInstance, configValues);
    }


}
