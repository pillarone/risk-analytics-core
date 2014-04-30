package org.pillarone.riskanalytics.core.components

import models.core.CoreModel
import static org.junit.Assert.*
import org.junit.Test
import org.pillarone.riskanalytics.core.simulation.item.Parameterization


class DataSourceDefinitionTests {

    @Test
    void testEqualsHashCode() {
        DataSourceDefinition d1 = new DataSourceDefinition(
                parameterization: new Parameterization("x", CoreModel),
                path: "path", fields: ['a', 'b'], periods: [0, 1], collectorName: "name"

        )
        DataSourceDefinition d2 = new DataSourceDefinition(
                parameterization: new Parameterization("x", CoreModel),
                path: "path", fields: ['a', 'b'], periods: [0, 1], collectorName: "name"

        )

        assertEquals(d1, d2)
        assertEquals(d1.hashCode(), d2.hashCode())

        d1 = new DataSourceDefinition(
                parameterization: new Parameterization("x", CoreModel),
                path: "path", fields: ['a', 'b'], periods: [0, 1], collectorName: "name"

        )
        d2 = new DataSourceDefinition(
                parameterization: new Parameterization("x", CoreModel),
                path: "path", fields: ['a'], periods: [0, 1], collectorName: "name"

        )

        assertFalse(d1 == d2)
        assertFalse(d1.hashCode() == d2.hashCode())
    }
}
