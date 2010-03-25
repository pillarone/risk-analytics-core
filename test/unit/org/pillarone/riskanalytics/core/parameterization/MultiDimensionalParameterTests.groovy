package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.model.Model
import models.core.CoreModel

class MultiDimensionalParameterTests extends GroovyTestCase {

    protected void setUp() {
        super.setUp();
        ConstraintsFactory.registerConstraint(new SimpleConstraint())
    }

    void testConstructor() {
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([1, 2, 3])
        assertTrue param.valuesConverted

        param = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]])
        assertFalse param.valuesConverted
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
        assertEquals([1, 2, 3], param.getValues())
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
        AbstractMultiDimensionalParameter param = new SimpleMultiDimensionalParameter([1, 2, 3])
        param.max_tokens = 2
        String output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList(['[1, 2]', '[3]']))"

        String parmStringValue = param.toString()
        assertTrue output.equals(parmStringValue)
        assertEquals GroovyUtils.toList(["[1, 2]", "[3]"]), [1, 2, 3]

        param = new SimpleMultiDimensionalParameter([[1, 2, 3], ["a", "b", "c"]])
        param.max_tokens = 2
        output = "new org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([['[1, 2]', '[3]'], ['[\"a\", \"b\"]', '[\"c\"]']]))"

        assertTrue output.equals(param.toString())
        assertEquals GroovyUtils.toList([["[1, 2]", "[3]"], ['["a", "b"]', '["c"]']]), [[1, 2, 3], ["a", "b", "c"]]

        param = new TableMultiDimensionalParameter([[1, 2, 3], [4, 5, 6], [7, 8, 9]], ['title1', 'title2', 'title3'])
        param.max_tokens = "[1, 2, 3]".length() + 1
        output = 'new org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([[1, 2, 3], [4, 5, 6], [7, 8, 9]]),["title1","title2","title3"])'

        assertTrue output.equals(param.toString())
        assertEquals GroovyUtils.toList([[1, 2, 3], [4, 5, 6], [7, 8, 9]]), [[1, 2, 3], [4, 5, 6], [7, 8, 9]]

        param = new MatrixMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]], ["row1", "row2"], ["col1", "col2"])
        param.max_tokens = 2
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

}