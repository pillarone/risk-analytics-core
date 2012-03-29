package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.PathMapping

class MappingCacheTests extends GroovyTestCase {

    PathMapping path1

    void setUp() {
        path1 = new PathMapping(pathName: "Empty:path1").save()
        assertNotNull path1
        assertNotNull new CollectorMapping(collectorName: "coll").save()
        assertNotNull new FieldMapping(fieldName: "field").save()
    }

    void testLookup() {
        MappingCache cache = MappingCache.instance

        assertEquals "Empty:path1", cache.lookupPath("Empty:path1").pathName
        assertEquals path1.id, cache.lookupPath("Empty:path1").id

        PathMapping path = cache.lookupPath("Empty:newPath")
        assertEquals "Empty:newPath", path.pathName
        assertNotNull path.id

    }
}
