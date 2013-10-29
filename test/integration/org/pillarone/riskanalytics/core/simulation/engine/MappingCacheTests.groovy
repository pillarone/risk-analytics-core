package org.pillarone.riskanalytics.core.simulation.engine

import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.output.PathMapping

import static org.junit.Assert.*

class MappingCacheTests {

    PathMapping path1

    @Before
    void setUp() {
        path1 = new PathMapping(pathName: "Empty:path1").save()
        assertNotNull path1
    }

    @Test
    void testLookup() {
        MappingCache cache = MappingCache.instance

        assertEquals "Empty:path1", cache.lookupPath("Empty:path1").pathName
        assertEquals path1.id, cache.lookupPath("Empty:path1").id

        PathMapping path = cache.lookupPath("Empty:newPath")
        assertEquals "Empty:newPath", path.pathName
        assertNotNull path.id

    }
}
