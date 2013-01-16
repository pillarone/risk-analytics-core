package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker

/**
 * Created with IntelliJ IDEA.
 * User: oandersson
 * Date: 1/15/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
class PeriodMatrixMultiDimensionalParameterTests extends GroovyTestCase {

    PeriodMatrixMultiDimensionalParameter parameter

    void setUp() {
        CoreModel model = new CoreModel()
        model.init()
        model.injectComponentNames()

        parameter = new PeriodMatrixMultiDimensionalParameter([[1, 0], [0, 1]], [['LOB1', 'LOB2'], ["1", "2"]], ITestComponentMarker)
        parameter.setSimulationModel(model)
    }

    void testGetValueAt() {

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

    void testSetValueAt() {
        parameter.setValueAt("Test", 2, 0)
        assertEquals("Test", parameter.getValueAt(2, 0))
        assertEquals("Test", parameter.getValueAt(0, 2))

        parameter.setValueAt("Test2", 3, 0)
        assertEquals("Test2", parameter.getValueAt(3, 0))
        assertEquals("Test2", parameter.getValueAt(0, 3))

        parameter.setValueAt(12, 2, 2)
        assertEquals(12, parameter.getValueAt(2, 2))

        parameter.setValueAt(10, 3, 2)
        assertEquals(10, parameter.getValueAt(3, 2))
        assertEquals(10, parameter.getValueAt(2, 3))

        parameter.setValueAt(15, 2, 3)
        assertEquals(15, parameter.getValueAt(2, 3))
        assertEquals(15, parameter.getValueAt(3, 2))


        parameter.setValueAt("1", 2, 1)
        assertEquals("1", parameter.getValueAt(2, 1))
        assertEquals("1", parameter.getValueAt(1, 2))

        parameter.setValueAt("2", 3, 1)
        assertEquals("2", parameter.getValueAt(3, 1))
        assertEquals("2", parameter.getValueAt(1, 3))
    }

    void testGetPossibleValues() {
        assertTrue(parameter.getPossibleValues(2, 0) instanceof List)
        assertTrue(parameter.getPossibleValues(2, 1) instanceof List)
        assertTrue(parameter.getPossibleValues(0, 2) instanceof List)
        assertTrue(parameter.getPossibleValues(1, 2) instanceof List)
        assertFalse(parameter.getPossibleValues(2, 2) instanceof List)

    }
}
