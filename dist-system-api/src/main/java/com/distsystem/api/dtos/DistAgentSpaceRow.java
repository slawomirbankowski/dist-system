package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentSpace", keyName="spaceName", keyIsUnique=true)
public class DistAgentSpaceRow extends BaseRow {

    private String spaceName;
    private String spaceOwner;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentSpaceRow(String spaceName, String spaceOwner, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.spaceName = spaceName;
        this.spaceOwner = spaceOwner;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentSpaceRow(String spaceName, String spaceOwner) {
        this.spaceName = spaceName;
        this.spaceOwner = spaceOwner;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public String getSpaceOwner() {
        return spaceOwner;
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
        return new Object[] { spaceName, spaceOwner, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentSpaceRow",
                "spaceName", spaceName,
                "spaceOwner", spaceOwner,
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
