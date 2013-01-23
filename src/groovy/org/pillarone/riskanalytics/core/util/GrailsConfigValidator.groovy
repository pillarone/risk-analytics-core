package org.pillarone.riskanalytics.core.util

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import grails.util.Environment

class GrailsConfigValidator {

    private static Log LOG = LogFactory.getLog(GrailsConfigValidator)

    /**
     * Checks a ConfigObject for certain entries and sets them to a default if they are null.
     * @param config The config object to check
     * @param A map whose keys are "." delimited nested keys which should be present in the config.
     *        The values are defaults value to which a key is set if it is currently null.
     */
    public static void validateConfig(ConfigObject config, Map<String, Object> necessaryContent) {
        Environment current = Environment.current
        for (Map.Entry<String, Object> entry in necessaryContent) {
            def value = getValueFromConfig(config, entry.key)
            if (value == null) {
                LOG.warn "Configuration warning: Value ${entry.key} not defined for environment ${current.name}. Setting to default."
                applyValueToConfig(config, entry.key, entry.value)
            }
        }
    }

    /**
     * Retrieves a value from a ConfigObject. If the key does not exist, null is returned.
     * @param configObject The config object to search.
     * @param pathToValue The key of the value. If the key is nested, they should be delimited by "."
     * @return The current value or null, if there is no value.
     */
    private static Object getValueFromConfig(ConfigObject configObject, String pathToValue) {
        String[] keys = pathToValue.split("\\.")
        Object currentValue = configObject
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i]
            if (currentValue instanceof ConfigObject && currentValue.containsKey(key)) {
                currentValue = currentValue.get(key)
            } else {
                return null
            }
        }
        return currentValue
    }

    /**
     * Add a nested value to a ConfigObject
     */
    private static void applyValueToConfig(ConfigObject configObject, String keyPath, def value) {
        String[] keys = keyPath.split("\\.")
        for (int i = 0; i < keys.length - 1; i++) {
            configObject[keys[i]] = new ConfigObject()
            configObject = configObject.get(keys[i])
        }
        configObject[keys[-1]] = value
    }

}