package org.pillarone.riskanalytics.core.parameter

import org.junit.Test
import org.pillarone.riskanalytics.core.parameterization.*

import static org.junit.Assert.*

class MultiDimensionalParameterTests {

    @Test
    void testSave() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: SimpleMultiDimensionalParameter.name, path: 'testSave')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        parameter = parameter.save()
        assertNotNull parameter

        assertEquals 1, parameter.multiDimensionalParameterValues.size()
        assertTrue parameter.multiDimensionalParameterValues.contains(paramValue)
        assertEquals 0, deserializeFromByteArray(paramValue.value)

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testSave')
        assertEquals 1, reloaded.multiDimensionalParameterValues.size()
    }

    @Test
    void testGetSimpleInstance() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: SimpleMultiDimensionalParameter.name, path: 'testGetSimpleInstance')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        parameter = parameter.save()
        assertNotNull parameter

        AbstractMultiDimensionalParameter amdp = parameter.getParameterInstance()
        assertTrue amdp instanceof SimpleMultiDimensionalParameter
        def values = amdp.values
        assertEquals 1, values.size()
        assertEquals 0, values[0][0]

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetSimpleInstance')
        assertEquals 1, reloaded.multiDimensionalParameterValues.size()
    }

    @Test
    void testGetSimpleInstanceWithMultipleColumns() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: SimpleMultiDimensionalParameter.name, path: 'testGetSimpleInstanceWithMultipleColumns')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        MultiDimensionalParameterValue paramValue2 = new MultiDimensionalParameterValue(row: 0, col: 1, value: serializeToByteArray(1))
        parameter.addToMultiDimensionalParameterValues(paramValue2)
        parameter = parameter.save()
        assertNotNull parameter

        AbstractMultiDimensionalParameter amdp = parameter.getParameterInstance()
        assertTrue amdp instanceof SimpleMultiDimensionalParameter
        def values = amdp.values
        assertEquals 2, values.size()
        assertEquals 0, values.get(0).get(0)
        assertEquals 1, values.get(1).get(0)

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetSimpleInstanceWithMultipleColumns')
        assertEquals 2, reloaded.multiDimensionalParameterValues.size()
    }


    @Test
    void testGetTableInstance() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: TableMultiDimensionalParameter.name, path: 'testGetTableInstance')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        MultiDimensionalParameterValue paramValue2 = new MultiDimensionalParameterValue(row: 0, col: 1, value: serializeToByteArray(1))
        parameter.addToMultiDimensionalParameterValues(paramValue2)
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 0, title: 'title 1'))
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 1, title: 'title 2'))
        parameter = parameter.save()
        assertNotNull parameter

        TableMultiDimensionalParameter mdp = parameter.getParameterInstance()
        def values = mdp.values
        assertEquals 2, values.size()
        assertEquals 0, values.get(0).get(0)
        assertEquals 1, values.get(1).get(0)

        assertEquals 2, mdp.columnNames.size()
        assertEquals 'title 1', mdp.columnNames[0]
        assertEquals 'title 2', mdp.columnNames[1]

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetTableInstance')
        assertEquals 2, reloaded.multiDimensionalParameterValues.size()
        assertEquals 2, reloaded.multiDimensionalParameterTitles.size()
    }

    @Test
    void testGetMatrixInstance() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: MatrixMultiDimensionalParameter.name, path: 'testGetMatrixInstance')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        MultiDimensionalParameterValue paramValue2 = new MultiDimensionalParameterValue(row: 0, col: 1, value: serializeToByteArray(1))
        parameter.addToMultiDimensionalParameterValues(paramValue2)
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 1, title: 'column title'))
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 1, col: 0, title: 'row title'))
        parameter = parameter.save()
        assertNotNull parameter

        MatrixMultiDimensionalParameter mdp = parameter.getParameterInstance()
        def values = mdp.values
        assertEquals 2, values.size()
        assertEquals 0, values.get(0).get(0)
        assertEquals 1, values.get(1).get(0)

        assertEquals 1, mdp.columnNames.size()
        assertEquals 'column title', mdp.columnNames[0]
        assertEquals 'row title', mdp.rowNames[0]

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetMatrixInstance')
        assertEquals 2, reloaded.multiDimensionalParameterValues.size()
        assertEquals 2, reloaded.multiDimensionalParameterTitles.size()
    }

    @Test
    void testGetLobInstance() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: ComboBoxMatrixMultiDimensionalParameter.name, path: 'testGetLobInstance')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        MultiDimensionalParameterValue paramValue2 = new MultiDimensionalParameterValue(row: 0, col: 1, value: serializeToByteArray(1))
        parameter.addToMultiDimensionalParameterValues(paramValue2)
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 0, title: 'title'))
        parameter = parameter.save()
        assertNotNull parameter

        ComboBoxMatrixMultiDimensionalParameter mdp = parameter.getParameterInstance()
        def values = mdp.values
        assertEquals 2, values.size()
        assertEquals 0, values.get(0).get(0)
        assertEquals 1, values.get(1).get(0)

        assertEquals 1, mdp.columnNames.size()
        assertEquals 'title', mdp.columnNames[0]
        assertEquals 'title', mdp.rowNames[0]

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetLobInstance')
        assertEquals 2, reloaded.multiDimensionalParameterValues.size()
        assertEquals 1, reloaded.multiDimensionalParameterTitles.size()
    }

    @Test
    void testGetLobTableInstance() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: ComboBoxTableMultiDimensionalParameter.name, path: 'testGetLobTableInstance')
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 0, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        MultiDimensionalParameterValue paramValue2 = new MultiDimensionalParameterValue(row: 0, col: 1, value: serializeToByteArray(1))
        parameter.addToMultiDimensionalParameterValues(paramValue2)
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 0, title: 'title 1'))
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 1, title: 'title 2'))
        parameter = parameter.save()
        assertNotNull parameter

        ComboBoxTableMultiDimensionalParameter mdp = parameter.getParameterInstance()
        def values = mdp.values
        assertEquals 2, values.size()
        assertEquals 0, values.get(0).get(0)
        assertEquals 1, values.get(1).get(0)

        assertEquals 2, mdp.columnNames.size()
        assertEquals 'title 1', mdp.columnNames[0]
        assertEquals 'title 2', mdp.columnNames[1]

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetLobTableInstance')
        assertEquals 2, reloaded.multiDimensionalParameterValues.size()
        assertEquals 2, reloaded.multiDimensionalParameterTitles.size()
    }

    @Test
    void testGetConstrainedInstance() {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(className: ConstrainedMultiDimensionalParameter.name, path: 'testGetConstrainedInstance')
        parameter.constraintName = SimpleConstraint.IDENTIFIER
        MultiDimensionalParameterValue paramValue = new MultiDimensionalParameterValue(row: 1, col: 0, value: serializeToByteArray(0))
        parameter.addToMultiDimensionalParameterValues(paramValue)
        MultiDimensionalParameterValue paramValue2 = new MultiDimensionalParameterValue(row: 1, col: 1, value: serializeToByteArray(1))
        parameter.addToMultiDimensionalParameterValues(paramValue2)
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 0, title: 'column title'))
        parameter.addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(row: 0, col: 1, title: 'column title 2'))
        parameter = parameter.save()
        assertNotNull parameter

        ConstrainedMultiDimensionalParameter mdp = parameter.getParameterInstance()
        assertEquals SimpleConstraint.IDENTIFIER, mdp.constraints.name
        def values = mdp.values
        assertEquals 2, values.size()
        assertEquals 0, values.get(0).get(0)
        assertEquals 1, values.get(1).get(0)

        assertEquals 2, mdp.columnNames.size()
        assertEquals 'column title', mdp.columnNames[0]
        assertEquals 'column title 2', mdp.columnNames[1]

        parameter.discard()
        def reloaded = MultiDimensionalParameter.findByPath('testGetConstrainedInstance')
        assertEquals 2, reloaded.multiDimensionalParameterValues.size()
        assertEquals 2, reloaded.multiDimensionalParameterTitles.size()
    }

    @Test
    void testSetInstance() {
        def mdp = new SimpleMultiDimensionalParameter([1, 2, 3])
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertEquals SimpleMultiDimensionalParameter.name, parameter.className
        assertEquals 3, parameter.multiDimensionalParameterValues.size()

        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 0 && it.value == serializeToByteArray(1)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 0 && it.value == serializeToByteArray(2)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 2 && it.col == 0 && it.value == serializeToByteArray(3)}

        mdp = new SimpleMultiDimensionalParameter([[1, 2], [3, 4]])
        parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertEquals SimpleMultiDimensionalParameter.name, parameter.className
        assertEquals 4, parameter.multiDimensionalParameterValues.size()

        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 0 && it.value == serializeToByteArray(1)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 0 && it.value == serializeToByteArray(2)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 1 && it.value == serializeToByteArray(3)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 1 && it.value == serializeToByteArray(4)}

        mdp = new TableMultiDimensionalParameter([[1, 2], [3, 4]], ['title 1', 'title 2'])
        parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertEquals TableMultiDimensionalParameter.name, parameter.className
        assertEquals 4, parameter.multiDimensionalParameterValues.size()
        assertEquals 2, parameter.multiDimensionalParameterTitles.size()

        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 0 && it.value == serializeToByteArray(1)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 0 && it.value == serializeToByteArray(2)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 1 && it.value == serializeToByteArray(3)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 1 && it.value == serializeToByteArray(4)}

        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 0 && it.col == 0 && it.title == 'title 1'}
        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 0 && it.col == 1 && it.title == 'title 2'}

        mdp = new ConstrainedMultiDimensionalParameter([[1, 2], [3, 4]], ['title 1', 'title 2'], ConstraintsFactory.getConstraints(SimpleConstraint.IDENTIFIER))
        parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertEquals ConstrainedMultiDimensionalParameter.name, parameter.className
        assertEquals SimpleConstraint.IDENTIFIER, parameter.constraintName
        assertEquals 4, parameter.multiDimensionalParameterValues.size()
        assertEquals 2, parameter.multiDimensionalParameterTitles.size()

        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 0 && it.value == serializeToByteArray(1)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 0 && it.value == serializeToByteArray(2)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 1 && it.value == serializeToByteArray(3)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 1 && it.value == serializeToByteArray(4)}

        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 0 && it.col == 0 && it.title == 'title 1'}
        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 0 && it.col == 1 && it.title == 'title 2'}

        mdp = new MatrixMultiDimensionalParameter([[1, 2], [3, 4]], ['row 1', 'row 2'], ['col 1', 'col 2'])
        parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertEquals MatrixMultiDimensionalParameter.name, parameter.className
        assertEquals 4, parameter.multiDimensionalParameterValues.size()
        assertEquals 4, parameter.multiDimensionalParameterTitles.size()

        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 0 && it.value == serializeToByteArray(1)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 0 && it.value == serializeToByteArray(2)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 0 && it.col == 1 && it.value == serializeToByteArray(3)}
        assertNotNull parameter.multiDimensionalParameterValues.find {it.row == 1 && it.col == 1 && it.value == serializeToByteArray(4)}

        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 0 && it.col == 1 && it.title == 'col 1'}
        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 0 && it.col == 2 && it.title == 'col 2'}
        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 1 && it.col == 0 && it.title == 'row 1'}
        assertNotNull parameter.multiDimensionalParameterTitles.find {it.row == 2 && it.col == 0 && it.title == 'row 2'}
    }

    @Test
    void testUpdateValues() {
        SimpleMultiDimensionalParameter mdp = new SimpleMultiDimensionalParameter([[1, 2], [3, 4]])

        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertNotNull parameter.save()

        int count = MultiDimensionalParameterValue.count()

        SimpleMultiDimensionalParameter newMdp = new SimpleMultiDimensionalParameter([[5, 6], [7, 8]])
        parameter.parameterInstance = newMdp

        assertNotNull parameter.save()

        assertEquals count, MultiDimensionalParameterValue.count()
        assertEquals newMdp.values, parameter.parameterInstance.values
    }

    @Test
    void testUpdateAndAddValues() {
        SimpleMultiDimensionalParameter mdp = new SimpleMultiDimensionalParameter([[1, 2], [3, 4]])

        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertNotNull parameter.save()

        int count = MultiDimensionalParameterValue.count()

        SimpleMultiDimensionalParameter newMdp = new SimpleMultiDimensionalParameter([[5, 6, 7], [8, 9, 10]])
        parameter.parameterInstance = newMdp

        assertNotNull parameter.save()

        assertEquals count + 2, MultiDimensionalParameterValue.count()
        assertEquals newMdp.values, parameter.parameterInstance.values
    }

    @Test
    void testUpdateAndRemoveValues() {
        SimpleMultiDimensionalParameter mdp = new SimpleMultiDimensionalParameter([[1, 2, 3], [4, 5, 6]])

        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertNotNull parameter.save()

        int count = MultiDimensionalParameterValue.count()

        SimpleMultiDimensionalParameter newMdp = new SimpleMultiDimensionalParameter([[7, 8], [9, 10]])
        parameter.parameterInstance = newMdp

        assertNotNull parameter.save()

        assertEquals count - 2, MultiDimensionalParameterValue.count()
        assertEquals newMdp.values, parameter.parameterInstance.values
    }

    @Test
    void testUpdateTitles() {
        MatrixMultiDimensionalParameter mdp = new MatrixMultiDimensionalParameter([[1, 2], [3, 4]], ['row1', 'row2'], ['col1', 'col2'])

        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertNotNull parameter.save()

        int count = MultiDimensionalParameterTitle.count()

        MatrixMultiDimensionalParameter newMdp = new MatrixMultiDimensionalParameter([[1, 2], [3, 4]], ['new1', 'new2'], ['new3', 'new4'])
        parameter.parameterInstance = newMdp

        assertNotNull parameter.save()

        assertEquals count, MultiDimensionalParameterTitle.count()
        assertEquals newMdp.rowNames, parameter.parameterInstance.rowNames
        assertEquals newMdp.columnNames, parameter.parameterInstance.columnNames
    }

    @Test
    void testUpdateAndAddTitles() {
        MatrixMultiDimensionalParameter mdp = new MatrixMultiDimensionalParameter([[1, 2], [3, 4]], ['row1', 'row2'], ['col1', 'col2'])

        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertNotNull parameter.save()

        int count = MultiDimensionalParameterTitle.count()

        MatrixMultiDimensionalParameter newMdp = new MatrixMultiDimensionalParameter([[1, 2, 3], [4, 5, 6], [7, 8, 9]], ['new1', 'new2', 'new3'], ['new4', 'new5', 'new6'])
        parameter.parameterInstance = newMdp

        assertNotNull parameter.save()

        assertEquals count + 2, MultiDimensionalParameterTitle.count()
        assertEquals newMdp.rowNames, parameter.parameterInstance.rowNames
        assertEquals newMdp.columnNames, parameter.parameterInstance.columnNames
    }

    @Test
    void testUpdateAndRemoveTitles() {
        MatrixMultiDimensionalParameter mdp = new MatrixMultiDimensionalParameter([[1, 2, 3], [4, 5, 6], [7, 8, 9]], ['new1', 'new2', 'new3'], ['new4', 'new5', 'new6'])

        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: 'path')
        parameter.parameterInstance = mdp

        assertNotNull parameter.save()

        int count = MultiDimensionalParameterTitle.count()

        MatrixMultiDimensionalParameter newMdp = new MatrixMultiDimensionalParameter([[1, 2], [4, 5]], ['new1', 'new2'], ['new3', 'new4'])
        parameter.parameterInstance = newMdp

        assertNotNull parameter.save()

        assertEquals count - 2, MultiDimensionalParameterTitle.count()
        assertEquals newMdp.rowNames, parameter.parameterInstance.rowNames
        assertEquals newMdp.columnNames, parameter.parameterInstance.columnNames
    }



    private byte[] serializeToByteArray(Serializable object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ObjectOutputStream oos = new ObjectOutputStream(baos)
        oos.writeObject(object)
        return baos.toByteArray()
    }

    private Object deserializeFromByteArray(byte[] array) {
        ByteArrayInputStream bais = new ByteArrayInputStream(array)
        ObjectInputStream ois = new ObjectInputStream(bais)
        return ois.readObject()
    }

}