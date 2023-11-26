package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;

import java.time.LocalDateTime;
import java.util.Map;

/** row for measure definition
 * measure is list of values measured over time */
public class DistAgentMeasureRow extends BaseRow {

    private final String measureName;
    private final String measureType;
    private final String measureParameters;

    protected int isActive;
    protected LocalDateTime createdDate;
    protected LocalDateTime lastUpdatedDate;

    public DistAgentMeasureRow(String measureName, String measureType, String measureParameters, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.measureName = measureName;
        this.measureType = measureType;
        this.measureParameters = measureParameters;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
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
        return new Object[] { measureName, measureType, measureParameters, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("measureName", measureName,
                "measureType", measureType,
                "measureParameters", measureParameters,
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
