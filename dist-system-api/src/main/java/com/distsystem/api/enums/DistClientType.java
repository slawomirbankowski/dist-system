package com.distsystem.api.enums;

/** all known types of client that are connecting agents */
public enum DistClientType {
    socket, // connections with SocketServer and Socket as client, direct, reliable and fast
    http, // HTTP interface
    kafka, // Kafka in the middle
    datagram, // Datagram - UDP packets, short, not reliable but very fast
    jdbc; // JDBC based with table in the middle with messages

}
