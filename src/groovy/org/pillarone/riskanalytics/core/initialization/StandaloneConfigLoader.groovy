package org.pillarone.riskanalytics.core.initialization

import groovy.transform.CompileStatic;
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.log4j.Log4jConfig

@CompileStatic
class StandaloneConfigLoader {

    static void loadLog4JConfig(String environment) {
        ConfigObject configObject = loadConfig(environment)
        Log4jConfig.initialize(configObject);
    }

    static def getValue(String environment, String key) {
        ConfigObject config = loadConfig(environment)
        return config.containsKey(key) ? config.get(key) : null
    }

    static IExternalDatabaseSupport getExternalDatabaseSupport(String env) {
        Class dbSupportClass = (Class) getValue(env, "databaseSupportClass");
        return (IExternalDatabaseSupport) dbSupportClass?.newInstance();
    }

    private static ConfigObject loadConfig(String environment) {
        Class configClass = Thread.currentThread().contextClassLoader.loadClass(GrailsApplication.CONFIG_CLASS)

        ConfigSlurper configSlurper = new ConfigSlurper(environment)
        ConfigObject configObject = configSlurper.parse(configClass)
        return configObject
    }
}
