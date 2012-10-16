package org.pillarone.riskanalytics.core.util

import org.codehaus.groovy.grails.commons.ConfigurationHolder

abstract class DatabaseUtils {

    public static boolean isOracleDatabase() {
        def url = ConfigurationHolder.config?.dataSource?.url
        if (url instanceof String) {
            return url.contains("oracle")
        }
        return false
    }
}
