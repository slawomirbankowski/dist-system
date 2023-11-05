package com.distsystem.api.enums;

/** type of environment as additional information */
public enum DistEnvironmentType {
    /** development */
    development,
    /** automated testing */
    testing,
    /** integration tests */
    integration,
    /** user acceptance testing */
    acceptance,
    /** */
    production;
    /** get default environment */
    public static DistEnvironmentType getDefault() {
        return development;
    }
}
