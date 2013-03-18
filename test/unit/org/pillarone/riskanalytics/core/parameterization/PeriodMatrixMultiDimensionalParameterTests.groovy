package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker


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

    void testUpdateTable() {
        parameter.updateTable(2, ["hierarchyOutputComponent"])
        assertEquals(2, parameter.titles[0].size())
        assertEquals(2, parameter.titles[1].size())
        assertEquals(2, parameter.values.size())
        assertEquals(2, parameter.values[0].size())
        assertEquals(2, parameter.values[1].size())

        assertEquals(1, parameter.getValueAt(2, 2))
        assertEquals(1, parameter.getValueAt(3, 3))
        assertEquals("hierarchyOutputComponent", parameter.getValueAt(2, 0))
        assertEquals("hierarchyOutputComponent", parameter.getValueAt(3, 0))
        assertEquals("1", parameter.getValueAt(2, 1))
        assertEquals("2", parameter.getValueAt(3, 1))

        parameter.updateTable(2, ["hierarchyOutputComponent", "exampleOutputComponent"])
        assertEquals(4, parameter.titles[0].size())
        assertEquals(4, parameter.titles[1].size())
        assertEquals(4, parameter.values.size())
        assertEquals(4, parameter.values[0].size())
        assertEquals(4, parameter.values[1].size())

        assertEquals(1, parameter.getValueAt(2, 2))
        assertEquals(1, parameter.getValueAt(3, 3))
        assertEquals(1, parameter.getValueAt(4, 4))
        assertEquals(1, parameter.getValueAt(5, 5))
        assertEquals("hierarchyOutputComponent", parameter.getValueAt(2, 0))
        assertEquals("hierarchyOutputComponent", parameter.getValueAt(3, 0))
        assertEquals("exampleOutputComponent", parameter.getValueAt(4, 0))
        assertEquals("exampleOutputComponent", parameter.getValueAt(5, 0))
        assertEquals("1", parameter.getValueAt(2, 1))
        assertEquals("2", parameter.getValueAt(3, 1))
        assertEquals("1", parameter.getValueAt(4, 1))
        assertEquals("2", parameter.getValueAt(5, 1))
    }

    void testValidateValues() {
        CoreModel model = new CoreModel()
        model.init()
        model.injectComponentNames()

        parameter = new PeriodMatrixMultiDimensionalParameter([[1, 0], [0, 1]], [['hierarchyOutputComponent', 'LOB2'], ["1", "1"]], ITestComponentMarker)
        parameter.setSimulationModel(model)

        parameter.validateValues()

        assertEquals(1, parameter.titles[0].size())
        assertEquals(1, parameter.titles[1].size())
        assertEquals(1, parameter.values.size())
        assertEquals(1, parameter.values[0].size())

        assertEquals("hierarchyOutputComponent", parameter.getValueAt(2, 0))
        assertEquals("1", parameter.getValueAt(2, 1))

        parameter = new PeriodMatrixMultiDimensionalParameter([[1, 0], [0, 1]], [['LOB1', 'LOB2'], ["1", "1"]], ITestComponentMarker)
        parameter.setSimulationModel(model)

        parameter.validateValues()

        assertEquals(0, parameter.titles[0].size())
        assertEquals(0, parameter.titles[1].size())
        assertEquals(0, parameter.values.size())
    }

    void testGetCorrelations() {
        parameter.updateTable(2, ['hierarchyOutputComponent', 'exampleOutputComponent'])
        List<PeriodMatrixMultiDimensionalParameter.CorrelationInfo> correlations = parameter.getCorrelations()
        assertEquals(16, correlations.size())
    }

    void testEmptyParameter() {
        CoreModel model = new CoreModel()
        model.init()
        model.injectComponentNames()
        PeriodMatrixMultiDimensionalParameter pmmdp = new PeriodMatrixMultiDimensionalParameter([], [[], []], ITestComponentMarker)
        pmmdp.setSimulationModel(model)
        assertEquals(0, pmmdp.getMaxPeriod())
        for (int row = 0; row < pmmdp.getRowCount(); row++) {
            for (int col = 0; col < pmmdp.getColumnCount(); col++) {
                assertEquals("", pmmdp.getValueAt(row, col))
            }
        }
    }
}
