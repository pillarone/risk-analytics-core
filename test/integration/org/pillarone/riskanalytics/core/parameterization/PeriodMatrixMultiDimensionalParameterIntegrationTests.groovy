package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.CoreModel
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.simulation.item.parameter.MultiDimensionalParameterHolder

/**
 * Created with IntelliJ IDEA.
 * User: oandersson
 * Date: 1/15/13
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
class PeriodMatrixMultiDimensionalParameterIntegrationTests extends GroovyTestCase {

    void testSaveLoad() {
        Parameterization parameterization = new Parameterization("Name", CoreModel)
        parameterization.addParameter(ParameterHolderFactory.getHolder(
                "path", 0, new PeriodMatrixMultiDimensionalParameter([[1, 0], [0, 1]], [['LOB1', 'LOB2'], ["1", "2"]], ITestComponentMarker)
        ))
        parameterization.save()

        parameterization = new Parameterization("Name", CoreModel)
        parameterization.load()

        MultiDimensionalParameterHolder param = parameterization.getParameters("path")[0]
        PeriodMatrixMultiDimensionalParameter parameter = param.businessObject
        assertEquals("LOB1", parameter.getValueAt(2, 0))
        assertEquals("LOB1", parameter.getValueAt(0, 2))
        assertEquals("LOB2", parameter.getValueAt(3, 0))
        assertEquals("LOB2", parameter.getValueAt(0, 3))
        assertEquals("1", parameter.getValueAt(2, 1))
        assertEquals("1", parameter.getValueAt(1, 2))
        assertEquals("2", parameter.getValueAt(3, 1))
        assertEquals("2", parameter.getValueAt(1, 3))
        assertEquals("", parameter.getValueAt(0, 0))
        assertEquals(1, parameter.getValueAt(2, 2))
        assertEquals(0, parameter.getValueAt(2, 3))
        assertEquals(0, parameter.getValueAt(3, 2))
        assertEquals(1, parameter.getValueAt(3, 3))

    }
}
