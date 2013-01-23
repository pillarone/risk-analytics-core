package org.pillarone.riskanalytics.core.util

class GrailsConfigValidatorTests extends GroovyTestCase {

    void testSimpleValuesPresent() {
        ConfigObject configObject = new ConfigObject()
        configObject["someKey"] = "someValue"

        GrailsConfigValidator.validateConfig(configObject, ["someKey": "anotherValue"])

        assertTrue configObject.containsKey("someKey")
        assertEquals "someValue", configObject.get("someKey")
    }

    void testSimpleValuesNotPresent() {
        ConfigObject configObject = new ConfigObject()

        GrailsConfigValidator.validateConfig(configObject, ["someKey": "anotherValue"])

        assertTrue configObject.containsKey("someKey")
        assertEquals "anotherValue", configObject.get("someKey")
    }

    void testNestedValuesPresent() {
        ConfigObject configObject = new ConfigObject()
        configObject.someKey.anotherKey = "someValue"

        GrailsConfigValidator.validateConfig(configObject, ["someKey.anotherKey": "anotherValue"])

        assertTrue configObject.containsKey("someKey")
        configObject = configObject.someKey
        assertTrue  configObject.containsKey("anotherKey")
        assertEquals "someValue", configObject.get("anotherKey")
    }

    void testNestedValuesNotPresent() {
        ConfigObject configObject = new ConfigObject()

        GrailsConfigValidator.validateConfig(configObject, ["someKey.anotherKey": "anotherValue"])

        assertTrue configObject.containsKey("someKey")
        configObject = configObject.someKey
        assertTrue  configObject.containsKey("anotherKey")
        assertEquals "anotherValue", configObject.get("anotherKey")
    }

}