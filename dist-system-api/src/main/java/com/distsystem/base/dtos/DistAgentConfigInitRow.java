package com.distsystem.base.dtos;

import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
public class DistAgentConfigInitRow {

    public final String distname;
    public final String configname;
    public String configvalue;

    public DistAgentConfigInitRow(String distname, String configname, String configvalue) {
        this.distname = distname;
        this.configname = configname;
        this.configvalue = configvalue;
    }

    public static DistAgentConfigInitRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentConfigInitRow(
                m.getStringOrEmpty("distname"),
                m.getStringOrEmpty("configname"),
                m.getStringOrEmpty("configvalue")
        );
    }
}
