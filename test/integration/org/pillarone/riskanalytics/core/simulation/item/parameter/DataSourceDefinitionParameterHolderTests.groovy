package org.pillarone.riskanalytics.core.simulation.item.parameter

import models.core.CoreModel
import org.junit.Test
import org.pillarone.riskanalytics.core.components.DataSourceDefinition
import org.pillarone.riskanalytics.core.parameter.DataSourceParameter

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue


class DataSourceDefinitionParameterHolderTests {

    @Test
    void testCreate() {
        DataSourceParameter parameter = new DataSourceParameter(
                parameterizationName: "target",
                parameterizationVersion: "2",
                modelClassName: CoreModel.name,
                parameterizationPath: "path",
                fields: "a|b",
                periods: "0|1",
                collectorName: "collector"
        )

        DataSourceParameterHolder parameterHolder = new DataSourceParameterHolder(parameter)
        DataSourceDefinition definition = parameterHolder.definition

        assertEquals("target", definition.parameterization.name)
        assertEquals(CoreModel, definition.parameterization.modelClass)
        assertEquals("2", definition.parameterization.versionNumber.toString())
        assertEquals("path", definition.path)
        assertEquals(2, definition.fields.size())
        assertEquals(2, definition.periods.size())
        assertEquals("collector", definition.collectorName)

        DataSourceParameter newParameter = parameterHolder.createEmptyParameter()
        parameterHolder.applyToDomainObject(newParameter)

        assertEquals("target", newParameter.parameterizationName)
        assertEquals("2", newParameter.parameterizationVersion)
        assertEquals(CoreModel.name, newParameter.modelClassName)
        assertTrue("fields", newParameter.fields.contains('a') && newParameter.fields.contains('b') && newParameter.fields.contains('|'))
        assertTrue("periods", newParameter.periods.contains('0') && newParameter.periods.contains('1') && newParameter.periods.contains('|'))
        assertEquals("collector", newParameter.collectorName)
    }


}
