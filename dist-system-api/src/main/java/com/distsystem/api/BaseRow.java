package com.distsystem.api;

import com.distsystem.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class BaseRow {

    protected static final Logger log = LoggerFactory.getLogger(BaseRow.class);

    /** change object to String JSON */
    public final String toJson() {
        return JsonUtils.serialize(this);
    }
    /** get table of objects as values for insert row */
    public abstract Object[] toInsertRow();
    /** convert row into map */
    public abstract Map<String, String> toMap();

}
