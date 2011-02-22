package org.pillarone.riskanalytics.core.util

import org.joda.time.DateTime

class CloneSupportTests extends GroovyTestCase {

    void testDeepClone() {
        List test = ["String", new Integer(1), new Double(2), new Short(3 as short), new Long(4), new DateTime(), true, null]

        List cloned = CloneSupport.deepClone(test)
        assertNotSame test, cloned
        assertEquals test.size(), cloned.size()

        6.times {
            assertNotSame test[it + 1], cloned[it + 1]
            assertEquals test[it + 1], cloned[it + 1]
        }

        assertEquals null, cloned[-1]
    }
}
