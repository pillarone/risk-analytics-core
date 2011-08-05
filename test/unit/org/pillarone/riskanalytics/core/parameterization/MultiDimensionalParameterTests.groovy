package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.util.GroovyUtils

class MultiDimensionalParameterTests extends GroovyTestCase {

    protected void setUp() {
        super.setUp();
        ConstraintsFactory.registerConstraint(new SimpleConstraint())
    }

    void testConstructor() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([1, 2, 3])
        assertEquals "Z0C0", 1, param.getValueAt(0, 0)
        assertEquals "Z1C0", 2, param.getValueAt(1, 0)
        assertEquals "Z2C0", 3, param.getValueAt(2, 0)
        param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]])
        assertEquals "Z0C0", 1, param.getValueAt(0, 0)
        assertEquals "Z1C0", 2, param.getValueAt(1, 0)
        assertEquals "Z2C0", 3, param.getValueAt(2, 0)
        assertEquals "Z0C1", 4, param.getValueAt(0, 1)
        assertEquals "Z1C1", 5, param.getValueAt(1, 1)
        assertEquals "Z2C1", 6, param.getValueAt(2, 1)
    }

    void testRowCount() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([1, 2, 3])
        assertEquals(3, param.rowCount)

        param = new SimpleMultiDimensionalParameter([[1, 2], [4, 5]])
        assertEquals(2, param.rowCount)
    }

    void testColumnCount() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([1, 2, 3])
        assertEquals(1, param.columnCount)

        param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5]])
        assertEquals(2, param.columnCount)
    }

    void testGetValueAt() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5]])
        assertEquals(4, param.getValueAt(0, 1))
        assertEquals("", param.getValueAt(2, 1))
    }

    void testGetValue() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5]])
        assertEquals([[1, 2, 3], [4, 5]], param.getValues())
        param = new SimpleMultiDimensionalParameter([1, 2, 3])
        assertEquals([[1, 2, 3]], param.getValues())
    }

    void testSetValueAt() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5]])
        param.setValueAt(2.2, 0, 1)
        assertEquals(2.2, param.getValueAt(0, 1))

        param.setValueAt(6, 2, 1)
        assertEquals(6, param.getValueAt(2, 1))

        // column index out of bound
        shouldFail(IndexOutOfBoundsException, {param.setValueAt(7, 1, 2)})

        // row index out of bound
        shouldFail(IndexOutOfBoundsException, {param.setValueAt(7, 3, 0)})
    }

    void testIncreaseDimension() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]])

        assertEquals 3, param.rowCount
        assertEquals 2, param.columnCount

        param.dimension = new MultiDimensionalParameterDimension(3, 3)
        assertEquals 3, param.rowCount
        assertEquals 3, param.columnCount
        assertTrue param.values.every { it.size() == 3}

        param.dimension = new MultiDimensionalParameterDimension(3, 4)
        assertEquals 4, param.rowCount
        assertEquals 3, param.columnCount


        param = new SimpleMultiDimensionalParameter([1, 2, 3])

        assertEquals 3, param.rowCount
        assertEquals 1, param.columnCount

        param.dimension = new MultiDimensionalParameterDimension(2, 3)

        assertEquals 3, param.rowCount
        assertEquals 2, param.columnCount

        param.setValueAt(4, 0, 1)
        param.setValueAt(5, 1, 1)
        param.setValueAt(6, 2, 1)

    }

    void testIncreaseDimensionEmpty() {
        AbstractMultiDimensionalParameter param = new ConstrainedMultiDimensionalParameter([], ['title1', 'title2'], ConstraintsFactory.getConstraints(SimpleConstraint.IDENTIFIER))

        assertEquals 0, param.valueRowCount
        assertEquals 2, param.columnCount

        param.dimension = new MultiDimensionalParameterDimension(2, 1)
        assertEquals 1, param.valueRowCount
        assertEquals 2, param.columnCount
        assertEquals 2, param.values.size()
        assertTrue param.values.every { it.size() == 1}

    }

    void testDecreaseDimension() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]])

        assertEquals 3, param.rowCount
        assertEquals 2, param.columnCount

        param.dimension = new MultiDimensionalParameterDimension(2, 2)
        assertEquals 2, param.rowCount
        assertEquals 2, param.columnCount

        param.dimension = new MultiDimensionalParameterDimension(2, 1)
        assertEquals 1, param.rowCount
        assertEquals 2, param.columnCount

        param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5]])
        param.dimension = new MultiDimensionalParameterDimension(2, 2)
        assertEquals 2, param.rowCount
        assertEquals 2, param.columnCount
        junit.framework.Assert.assertEquals("second row not shrinked", 2, param.values[1].size())

        param = new MatrixMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]], ['row1', 'row2', 'row3'], ['col1', 'col2'])
        param.dimension = new MultiDimensionalParameterDimension(1, 2)
        assertEquals 3, param.rowCount
        assertEquals 2, param.columnCount

        assertEquals 2, param.getRowNames().size()
        assertEquals 1, param.getColumnNames().size()

        param = new ComboBoxMatrixMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]], ['col1', 'col2', 'col3'], ITestComponentMarker)
        param.dimension = new MultiDimensionalParameterDimension(2, 2)
        assertEquals 3, param.rowCount
        assertEquals 3, param.columnCount

        assertEquals 2, param.getRowNames().size()
        assertEquals 2, param.getColumnNames().size()

    }

    void testGetColumnByName() {
        TableMultiDimensionalParameter param = new TableMultiDimensionalParameter([[1], [2]], ['Col 1', 'Col 2'])

        List col1 = param.getColumnByName('Col 1')
        assertEquals 1, col1[0]

        List col2 = param.getColumnByName('Col 2')
        assertEquals 2, col2[0]
        shouldFail(IllegalArgumentException, {
            List col3 = param.getColumnByName('Col 3')
            assertEquals 2, col3[0]
        })
    }

    void testIsEditable() {
        AbstractMultiDimensionalParameter param = new MatrixMultiDimensionalParameter([1, 2, 3], ['Row'], ['Col'])

        assertFalse param.isCellEditable(0, 0)
        assertFalse param.isCellEditable(0, 1)
        assertFalse param.isCellEditable(4, 0)

        assertTrue param.isCellEditable(1, 1)

    }


    void testToString() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([])
        param.max_tokens = 2
        String output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([[]]))"

        String parmStringValue = param.toString()
        assertTrue output.equals(parmStringValue)
        assertEquals GroovyUtils.toList([[]]), []

        param = new SimpleMultiDimensionalParameter([1])
        param.max_tokens = 2
        output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1]']]))"

        parmStringValue = param.toString()
        assertTrue output.equals(parmStringValue)
        assertEquals GroovyUtils.toList(['[1]']), [1]

        param = new SimpleMultiDimensionalParameter([1, 2, 3, 4, 5])
        param.max_tokens = 2
        output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2, 3]', '[4, 5]']]))"

        parmStringValue = param.toString()
        assertTrue output.equals(parmStringValue)
        assertEquals GroovyUtils.toList(['[1, 2, 3]', '[4, 5]']), [1, 2, 3, 4, 5]

        param = new SimpleMultiDimensionalParameter([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11])
        param.max_tokens = 1
        output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2]', '[3]', '[4]', '[5]', '[6]', '[7]', '[8]', '[9]', '[10]', '[11]']]))"

        parmStringValue = param.toString()
        assertTrue output.equals(parmStringValue)
        assertEquals GroovyUtils.toList(['[1, 2]', '[3]', '[4]', '[5]', '[6]', '[7]', '[8]', '[9]', '[10]', '[11]']), [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]

        param = new SimpleMultiDimensionalParameter([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11])
        param.max_tokens = 2
        output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2, 3]', '[4, 5]', '[6, 7]', '[8, 9]', '[10, 11]']]))"

        parmStringValue = param.toString()
        assertTrue output.equals(parmStringValue)
        assertEquals GroovyUtils.toList(['[1, 2, 3]', '[4, 5]', '[6, 7]', '[8, 9]', '[10, 11]']), [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]

        param = new SimpleMultiDimensionalParameter([[1, 2, 3], ["a", "b", "c"]])
        param.max_tokens = 1
        output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2]', '[3]'], ['[\"a\", \"b\"]', '[\"c\"]']]))"

        assertTrue output.equals(param.toString())
        assertEquals GroovyUtils.toList([["[1, 2]", "[3]"], ['["a", "b"]', '["c"]']]), [[1, 2, 3], ["a", "b", "c"]]

        param = new TableMultiDimensionalParameter([[1, 2, 3], [4, 5, 6], [7, 8, 9]], ['title1', 'title2', 'title3'])
        param.max_tokens = "[1, 2, 3]".length() + 1
        output = 'new org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([[1, 2, 3], [4, 5, 6], [7, 8, 9]]),["title1","title2","title3"])'


        assertTrue output.equals(param.toString())
        assertEquals GroovyUtils.toList([[1, 2, 3], [4, 5, 6], [7, 8, 9]]), [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
        param = new MatrixMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]], ["row1", "row2"], ["col1", "col2"])
        param.max_tokens = 1
        output = "new org.pillarone.riskanalytics.core.parameterization.MatrixMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2]', '[3]'], ['[4, 5]', '[6]']]),[\"row1\",\"row2\"],[\"col1\",\"col2\"])"
        assertTrue output.equals(param.toString())
        assertEquals GroovyUtils.toList([["[1, 2]", "[3]"], ["[4, 5]", "[6]"]]), [[1, 2, 3], [4, 5, 6]]

        param = new ConstrainedMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]], ["row1", "row2"], ConstraintsFactory.getConstraints(SimpleConstraint.IDENTIFIER))
        param.max_tokens = 1
        output = "new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2]', '[3]'], ['[4, 5]', '[6]']]),[\"row1\",\"row2\"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('SIMPLE_CONSTRAINT'))"

        assertTrue output.equals(param.toString())
        assertEquals GroovyUtils.toList([['[1, 2]', '[3]'], ['[4, 5]', '[6]']]), [[1, 2, 3], [4, 5, 6]]
    }

    void testSetSimulationModel() {
        Model model = new CoreModel()
        model.init()
        model.injectComponentNames()

        AbstractMultiDimensionalParameter mdp = new ComboBoxMatrixMultiDimensionalParameter([[0, 1], [1, 0]], ['invalid name', 'hierarchy output component'], ITestComponentMarker)
        mdp.setSimulationModel(model)
        mdp.validateValues()

        assertFalse "invalid name" == mdp.getValueAt(0, 1)
        assertEquals "hierarchy output component", mdp.getValueAt(0, 2)

        mdp = new ComboBoxTableMultiDimensionalParameter([['invalid name', 'hierarchy output component']], ['title'], ITestComponentMarker)
        mdp.setSimulationModel(model)
        mdp.validateValues()

        assertFalse "invalid name" == mdp.getValueAt(1, 0)
        assertEquals "hierarchy output component", mdp.getValueAt(2, 0)
    }

    void testGetColumnIndex() {
        TableMultiDimensionalParameter mdp = new TableMultiDimensionalParameter([[1], [2]], ['title1', 'title2'])
        int index = mdp.getColumnIndex("title1")
        assertEquals "title1", mdp.getValueAt(0, index)

        index = mdp.getColumnIndex("title2")
        assertEquals "title2", mdp.getValueAt(0, index)
    }

    void testGetColumn() {
        TableMultiDimensionalParameter mdp = new TableMultiDimensionalParameter([[1, 3], [2, 4]], ['title1', 'title2'])
        assertEquals mdp.getValueAt(1, 0), mdp.getColumn(0)[0]
        assertEquals mdp.getValueAt(2, 0), mdp.getColumn(0)[1]

    }

    void testClone() {
        SimpleMultiDimensionalParameter param1 = new SimpleMultiDimensionalParameter([[0, 1], [1, 2]])
        SimpleMultiDimensionalParameter clone1 = param1.clone()

        assertNotSame param1.values, clone1.values
        for (int i = 0; i < param1.values.size(); i++) {
            assertNotSame param1.values[i], clone1.values[i]
        }
        assertEquals param1.values, clone1.values
        assertEquals param1.simulationModel, clone1.simulationModel

        TableMultiDimensionalParameter param2 = new TableMultiDimensionalParameter([[0, 1], [1, 2]], ['title', 'title2'])
        TableMultiDimensionalParameter clone2 = param2.clone()

        assertNotSame param2.values, clone2.values
        for (int i = 0; i < param2.values.size(); i++) {
            assertNotSame param2.values[i], clone2.values[i]
        }
        assertEquals param2.values, clone2.values
        assertEquals param2.simulationModel, clone2.simulationModel

        assertNotSame param2.titles, clone2.titles
        assertEquals param2.titles, clone2.titles

        MatrixMultiDimensionalParameter param3 = new MatrixMultiDimensionalParameter([[0, 1], [1, 2]], ['title', 'title2'], ['a', 'b'])
        MatrixMultiDimensionalParameter clone3 = param3.clone()

        assertNotSame param3.values, clone3.values
        for (int i = 0; i < param3.values.size(); i++) {
            assertNotSame param3.values[i], clone3.values[i]
        }
        assertEquals param3.values, clone3.values
        assertEquals param3.simulationModel, clone3.simulationModel

        assertNotSame param3.rowTitles, clone3.rowTitles
        assertEquals param3.rowTitles, clone3.rowTitles

        assertNotSame param3.columnTitles, clone3.columnTitles
        assertEquals param3.columnTitles, clone3.columnTitles

        ComboBoxMatrixMultiDimensionalParameter param4 = new ComboBoxMatrixMultiDimensionalParameter([[0, 1], [1, 2]], ['title', 'title2'], ITestComponentMarker)
        param4.comboBoxValues.put("x", "y")
        ComboBoxMatrixMultiDimensionalParameter clone4 = param4.clone()

        //clone should not this parameterization dependent map
        assertTrue clone4.comboBoxValues.isEmpty()
        assertNotSame param4.values, clone4.values
        for (int i = 0; i < param4.values.size(); i++) {
            assertNotSame param4.values[i], clone4.values[i]
        }
        assertEquals param4.values, clone4.values
        assertEquals param4.simulationModel, clone4.simulationModel

        assertNotSame param4.rowTitles, clone4.rowTitles
        assertEquals param4.rowTitles, clone4.rowTitles

        assertNotSame param4.columnTitles, clone4.columnTitles
        assertEquals param4.columnTitles, clone4.columnTitles

        assertEquals param4.markerClass, clone4.markerClass

        ComboBoxTableMultiDimensionalParameter param5 = new ComboBoxTableMultiDimensionalParameter([[0, 1], [1, 2]], ['title', 'title2'], ITestComponentMarker)
        param5.comboBoxValues.put("x", "y")
        ComboBoxTableMultiDimensionalParameter clone5 = param5.clone()

        //clone should not this parameterization dependent map
        assertTrue clone5.comboBoxValues.isEmpty()
        assertNotSame param5.values, clone5.values
        for (int i = 0; i < param5.values.size(); i++) {
            assertNotSame param5.values[i], clone5.values[i]
        }
        assertEquals param5.values, clone5.values
        assertEquals param5.simulationModel, clone5.simulationModel

        assertNotSame param5.titles, clone5.titles
        assertEquals param5.titles, clone5.titles

        assertEquals param5.markerClass, clone5.markerClass

        ConstrainedMultiDimensionalParameter param6 = new ConstrainedMultiDimensionalParameter([[0, 1], [1, 2]], ['title', 'title2'], ConstraintsFactory.getConstraints(SimpleConstraint.IDENTIFIER))
        param6.comboBoxValues.put("x", new HashMap())
        ConstrainedMultiDimensionalParameter clone6 = param6.clone()

        //clone should not this parameterization dependent map
        assertTrue clone6.comboBoxValues.isEmpty()
        assertNotSame param6.values, clone6.values
        for (int i = 0; i < param6.values.size(); i++) {
            assertNotSame param6.values[i], clone6.values[i]
        }
        assertEquals param6.values, clone6.values
        assertEquals param6.simulationModel, clone6.simulationModel

        assertNotSame param6.titles, clone6.titles
        assertEquals param6.titles, clone6.titles

        assertEquals param6.constraints, clone6.constraints
    }

}