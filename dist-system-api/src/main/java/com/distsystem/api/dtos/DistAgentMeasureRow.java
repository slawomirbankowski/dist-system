package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.Map;

/** row for measure definition
 * measure is list of values measured over time */
public class DistAgentMeasureRow extends BaseRow {

    private final String measureGuid;
    private final String measureName;
    private final String measureType;
    private final String measureParameters;
    /** 0 - object is not active, 1 - object is active */
    protected int isActive;
    protected LocalDateTime createdDate;
    protected LocalDateTime lastUpdatedDate;

    public DistAgentMeasureRow(String measureName, String measureType, String measureParameters, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.measureGuid = DistUtils.generateCustomGuid("MEASURE");
        this.measureName = measureName;
        this.measureType = measureType;
        this.measureParameters = measureParameters;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentMeasureRow(String measureName, String measureType, String measureParameters) {
        this.measureGuid = DistUtils.generateCustomGuid("MEASURE");
        this.measureName = measureName;
        this.measureType = measureType;
        this.measureParameters = measureParameters;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }
    public String getMeasureName() {
        return measureName;
    }

    public String getMeasureType() {
        return measureType;
    }

    public String getMeasureParameters() {
        return measureParameters;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { measureGuid, measureName, measureType, measureParameters, isActive, createdDate, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("measureName", measureName,
                "measureType", measureType,
                "measureParameters", measureParameters,
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "measureName";
    }
}
