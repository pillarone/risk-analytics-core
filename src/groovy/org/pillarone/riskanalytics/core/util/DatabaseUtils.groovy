package org.pillarone.riskanalytics.core.util

import org.codehaus.groovy.grails.commons.ConfigurationHolder

abstract class DatabaseUtils {

    public static boolean isOracleDatabase() {
        def config = ConfigurationHolder.config
        if(config == null) {
            return false
        }
        return config.dataSource.url.contains("oracle")
    }
}
