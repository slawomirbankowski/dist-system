package com.distsystem.api;

/** universal class for controller out status */
public class ControllerStatus {
    /** name of status */
    private String status;
    /** number of rows affected */
    private int rowsAffected;

    public ControllerStatus(String status, int rowsAffected) {
        this.status = status;
        this.rowsAffected = rowsAffected;
    }
    public ControllerStatus(String status) {
        this.status = status;
        this.rowsAffected = 0;
    }
    /** get status */
    public String getStatus() {
        return status;
    }
    public int getRowsAffected() {
        return rowsAffected;
    }

    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String CACHE_CLOSED = "CACHE_CLOSED";
    public static final String CACHE_NOT_FOUND = "CACHE_NOT_FOUND";

    /** controller template statuses */
    public static ControllerStatus statusOk = new ControllerStatus(STATUS_OK);
    public static ControllerStatus statusError = new ControllerStatus(STATUS_ERROR);
    public static ControllerStatus statusCacheClosed = new ControllerStatus(CACHE_CLOSED);
    public static ControllerStatus statusCacheNotFound = new ControllerStatus(CACHE_NOT_FOUND);

}
