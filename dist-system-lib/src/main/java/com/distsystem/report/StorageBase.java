package com.distsystem.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class for any storage like JDBC, Elasticsearch, Redis, MongoDB, FTP, HTTP, ...
 *  */
public abstract class StorageBase  {

    /** local logger for this clas s*/
    protected static final Logger log = LoggerFactory.getLogger(StorageBase.class);


}
