package com.distsystem.interfaces;

import java.util.Properties;

/** interface for configuration listener to be called when configuration is changing */
public interface ConfigListener {
    /** change configuration values */
    void onConfigChange(long seq, Properties pr);
}
