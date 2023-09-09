package com.distsystem.interfaces;

/** interface for Storage manager to maintain storages in distributed environment.
 * Each storage could be JDBC compliant database, Elasticsearch, MongoDB, Redis, FTP, ...
 * Storages could be defined to have Connection Pools.
 * */
public interface Storages extends DistService {
}
