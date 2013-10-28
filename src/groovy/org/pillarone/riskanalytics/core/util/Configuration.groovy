package org.pillarone.riskanalytics.core.util

import grails.util.Holders

abstract class Configuration {

    public static boolean getBoolean(String key, boolean defaultValue) {
        ConfigObject config = Holders.config
        if (config != null) {
            if (config.containsKey(key)) {
                def value = config[key]
                if (value instanceof Boolean) {
                    return value
                }
            }
        }

        return defaultValue
    }
}
