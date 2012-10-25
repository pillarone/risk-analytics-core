package org.pillarone.riskanalytics.core.util

import org.codehaus.groovy.grails.commons.ConfigurationHolder


abstract class Configuration {

    public static boolean getBoolean(String key, boolean defaultValue) {
        ConfigObject config = ConfigurationHolder.config
        if(config.containsKey(key)) {
            def value = config[key]
            if(value instanceof Boolean) {
                return value
            }
        }

        return defaultValue
    }
}
