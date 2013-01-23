package org.pillarone.riskanalytics.core.util

import org.pillarone.riskanalytics.core.util.ConfigObjectUtils

class ConfigObjectUtilsTests extends GroovyTestCase {

    void testSpreadRanges() {

        ConfigObject parameter = new ConfigObject()
        parameter[0..2] = "Foo"
        ConfigObjectUtils.spreadRanges(parameter)
        assertEquals "Foo", parameter[0]
        assertEquals "Foo", parameter[1]
        assertEquals "Foo", parameter[2]
        assertFalse parameter.containsKey([0..2])


        parameter = new ConfigObject()
        parameter[0..2] = "Foo"
        parameter.nextLevel[0..4] = "Bar"
        ConfigObjectUtils.spreadRanges(parameter)

        assertEquals "Foo", parameter[0]
        assertEquals "Foo", parameter[1]
        assertEquals "Foo", parameter[2]
        assertFalse parameter.containsKey([0..2])

        assertEquals "Bar", parameter.nextLevel[0]
        assertEquals "Bar", parameter.nextLevel[1]
        assertEquals "Bar", parameter.nextLevel[2]
        assertEquals "Bar", parameter.nextLevel[3]
        assertEquals "Bar", parameter.nextLevel[4]
        assertFalse parameter.nextLevel.containsKey([0..4])

    }
}