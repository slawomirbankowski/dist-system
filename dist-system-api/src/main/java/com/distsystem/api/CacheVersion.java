package com.distsystem.api;

/** information about version of dist-cache application OR any other dist-cache component */
public class CacheVersion {
    private String version;
    private String[] apiVersions;
    private String application;

    public CacheVersion(String version, String[] apiVersions, String application) {
        this.version = version;
        this.apiVersions = apiVersions;
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public String[] getApiVersions() {
        return apiVersions;
    }

    public String getApplication() {
        return application;
    }

    /** current version of application */
    public final static CacheVersion current = new CacheVersion("", new String[] { "v1"}, "dist-cache-app");
}

