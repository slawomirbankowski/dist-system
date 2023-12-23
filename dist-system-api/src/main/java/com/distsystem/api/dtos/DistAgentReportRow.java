package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentReportRow extends BaseRow {

    private String reportName;
    private String reportDescription;
    private String reportType;
    private String reportQuery;
    private String storageName;
    private String columnList;
    private String filterList;
    protected int isActive;
    protected LocalDateTime createdDate;
    protected LocalDateTime lastUpdatedDate;

    public DistAgentReportRow(String reportName, String reportDescription, String reportType, String reportQuery, String storageName, String columnList, String filterList, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.reportName = reportName;
        this.reportDescription = reportDescription;
        this.reportType = reportType;
        this.reportQuery = reportQuery;
        this.storageName = storageName;
        this.columnList = columnList;
        this.filterList = filterList;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentReportRow(String reportName, String reportDescription, String reportType, String reportQuery, String storageName, String columnList, String filterList) {
        this.reportName = reportName;
        this.reportDescription = reportDescription;
        this.reportType = reportType;
        this.reportQuery = reportQuery;
        this.storageName = storageName;
        this.columnList = columnList;
        this.filterList = filterList;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { reportName, reportDescription, reportType, reportQuery, storageName, columnList, filterList, isActive, createdDate, lastUpdatedDate };
    }

    public Map<String, String> toMap() {
        return Map.of("reportName", reportName,
                "reportDescription", reportDescription,
                "reportType", reportType,
                "reportQuery", reportQuery,
                "storageName", storageName,
                "columnList", columnList,
                "filterList", filterList,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    public static DistAgentReportRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentReportRow(
                m.getString("reportName", ""),
                m.getString("reportDescription", ""),
                m.getString("reportType", ""),
                m.getString("reportQuery", ""),
                m.getString("storageName", ""),
                m.getString("columnList", ""),
                m.getString("filterList", ""),
                m.getInt("isActive", 1),
                m.getLocalDateOrNow("createdDate"),
                m.getLocalDateOrNow("lastUpdatedDate")
        );
    }
    public String getReportName() {
        return reportName;
    }
    public String getReportDescription() {
        return reportDescription;
    }
    public String getReportType() {
        return reportType;
    }
    public String getReportQuery() {
        return reportQuery;
    }
    public String getStorageName() {
        return storageName;
    }
    public String getColumnList() {
        return columnList;
    }
    public String getFilterList() {
        return filterList;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

}
