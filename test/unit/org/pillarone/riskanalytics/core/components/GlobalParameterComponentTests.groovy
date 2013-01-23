package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.example.component.ExampleParameterComponent
import java.lang.reflect.Method


class GlobalParameterComponentTests extends GroovyTestCase {

    void testGetMethods() {
        GlobalParameterComponent component = new ExampleParameterComponent()
        Map<String, Method> methods = component.globalMethods

        assertEquals 3, methods.size()

        Method m1 = methods.get("int")
        assertNotNull m1
        assertEquals "getInteger", m1.name

        Method m2 = methods.get("string")
        assertNotNull m2
        assertEquals "getString", m2.name

        Method m3 = methods.get("sanitychecks")
        assertNotNull m3
        assertEquals "getSanityChecks", m3.name
    }
}
