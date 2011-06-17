package org.pillarone.riskanalytics.core.util

import org.codehaus.groovy.grails.commons.ConfigurationHolder

abstract class DatabaseUtils {

    public static boolean isOracleDatabase() {
        return ConfigurationHolder.config.dataSource.url.contains("oracle")
    }
}
