package com.distsystem.api;

import com.distsystem.interfaces.ConfigListener;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Set;

/** listener to configuration change with */
public class DistConfigChangeListener {

    /** created date of this change listener  */
    private LocalDateTime createDate = LocalDateTime.now();
    /** parent configuration */
    private DistConfig parentConfig;
    private String nameContains;
    /** listener to notify if there is a configuration change */
    private ConfigListener listener;

    /** */
    DistConfigChangeListener(DistConfig parentConfig, String nameContains, ConfigListener listener) {
        this.parentConfig = parentConfig;
        this.nameContains = nameContains;
        this.listener = listener;
    }
    /** check if this listener is subscribed for this change */
    private boolean check(String name) {
        return name.contains(nameContains);
    }
    /** check multiple names of parameters */
    private boolean check(Set<Object> names) {
        return names.stream()
                .map(Object::toString)
                .anyMatch(n -> n.contains(nameContains));
    }
    /** change of configuration - just one value */
    public void changeConfiguration(long seq, String name, Object oldValue, String newValue) {
        if (check(name)) {
            Properties pr = new Properties();
            pr.setProperty(name, newValue);
            if (oldValue.equals(newValue)) {

            }
            listener.onConfigChange(seq, pr);
        }
    }
    /** change of configuration - multiple values */
    public void changeConfiguration(long seq, Properties oldProps, Properties newProps) {
        if (check(oldProps.keySet())) {
            listener.onConfigChange(seq, newProps);
        }
    }
}
