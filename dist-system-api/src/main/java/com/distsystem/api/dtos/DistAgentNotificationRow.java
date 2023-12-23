package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentNotification", keyName="notificationName", keyIsUnique=true)
public class DistAgentNotificationRow extends BaseRow {

    private final String notificationName;
    private String notificationType;
    private String notificationParams;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentNotificationRow(String notificationName, String notificationType, String notificationParams, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.notificationName = notificationName;
        this.notificationType = notificationType;
        this.notificationParams = notificationParams;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentNotificationRow(String notificationName, String notificationType, String notificationParams) {
        this.notificationName = notificationName;
        this.notificationType = notificationType;
        this.notificationParams = notificationParams;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getNotificationName() {
        return notificationName;
    }
    public String getNotificationType() {
        return notificationType;
    }
    public String getNotificationParams() {
        return notificationParams;
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
        return new Object[] { notificationName, notificationType, notificationParams, isActive, createdDate, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("notificationName", notificationName,
                "notificationType", notificationType,
                "notificationParams", notificationParams,
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
