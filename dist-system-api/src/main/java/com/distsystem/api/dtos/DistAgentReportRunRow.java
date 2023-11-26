package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentReportRunRow", keyName="runGuid", keyIsUnique=true)
public class DistAgentReportRunRow extends BaseRow {

    private String runGuid;
    private String reportName;
    private String reportParams;
    private String formatName;
    private LocalDateTime runDate;
    private String runStatus;
    private long rowsCount;
    private long columnsCount;
    private long contentSize;
    private String contentPath;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentReportRunRow(String runGuid, String reportName, String reportParams, String formatName, LocalDateTime runDate, String runStatus, long rowsCount, long columnsCount, long contentSize, String contentPath, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.runGuid = runGuid;
        this.reportName = reportName;
        this.reportParams = reportParams;
        this.formatName = formatName;
        this.runDate = runDate;
        this.runStatus = runStatus;
        this.rowsCount = rowsCount;
        this.columnsCount = columnsCount;
        this.contentSize = contentSize;
        this.contentPath = contentPath;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentReportRunRow(String runGuid, String reportName, String reportParams, String formatName, LocalDateTime runDate, String runStatus, long rowsCount, long columnsCount, long contentSize, String contentPath) {
        this.runGuid = runGuid;
        this.reportName = reportName;
        this.reportParams = reportParams;
        this.formatName = formatName;
        this.runDate = runDate;
        this.runStatus = runStatus;
        this.rowsCount = rowsCount;
        this.columnsCount = columnsCount;
        this.contentSize = contentSize;
        this.contentPath = contentPath;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public String getRunGuid() {
        return runGuid;
    }

    public String getReportName() {
        return reportName;
    }

    public String getReportParams() {
        return reportParams;
    }

    public String getFormatName() {
        return formatName;
    }

    public LocalDateTime getRunDate() {
        return runDate;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public long getRowsCount() {
        return rowsCount;
    }

    public long getColumnsCount() {
        return columnsCount;
    }

    public long getContentSize() {
        return contentSize;
    }

    public String getContentPath() {
        return contentPath;
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
        return new Object[] { runGuid, reportName, reportParams, formatName, runDate, runStatus, rowsCount, columnsCount, contentSize, contentPath, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        Map<String, String> m = Map.of("", "");
        m.putAll(Map.of("runGuid", runGuid,
                "reportName", reportName,
                "reportParams", reportParams,
                "runDate", runDate.toString(),
                "runStatus", runStatus,
                "rowsCount", ""+rowsCount,
                "columnsCount", ""+columnsCount,
                "contentSize", ""+contentSize,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString()));
        return m;
    }

}
