package org.pillarone.riskanalytics.core.util

import grails.util.Holders

abstract class DatabaseUtils {

    public static boolean isOracleDatabase() {
        def url = Holders.config?.dataSource?.url
        if (url instanceof String) {
            return url.contains("oracle")
        }
        return false
    }

    public static boolean isMsSqlDatabase() {
        def url = Holders.config?.dataSource?.url
        if (url instanceof String) {
            return url.contains("jtds")
        }
        return false
    }

    public static boolean isMySqlDatabase() {
        def url = Holders.config?.dataSource?.url
        if (url instanceof String) {
            return url.contains("mysql")
        }
        return false
    }

}
