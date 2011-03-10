package org.pillarone.riskanalytics.core.parameter

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.*
import org.springframework.jdbc.datasource.DataSourceUtils
import java.sql.Connection

class MultiDimensionalParameter extends Parameter {

    String className
    String constraintName
    String markerClassName
    Set<MultiDimensionalParameterValue> multiDimensionalParameterValues
    Set<MultiDimensionalParameterTitle> multiDimensionalParameterTitles

    Object parameterObject

    javax.sql.DataSource dataSource

    static transients = ['parameterObject', 'dataSource']

    static hasMany = [multiDimensionalParameterValues: MultiDimensionalParameterValue,
            multiDimensionalParameterTitles: MultiDimensionalParameterTitle]

    static constraints = {
        markerClassName(nullable: true)
        constraintName(nullable: true)
    }

    public void setParameterInstance(Object value) {
        value = value as AbstractMultiDimensionalParameter
        this.className = value.class.name
        extractValues(value.values)
        int offset = value.titleColumnCount > 0 && value.titleRowCount > 0 ? 1 : 0
        extractRowTitles(value.rowNames, offset)
        extractColumnTitles(value.columnNames, offset)
        parameterObject = null
        markerClassName = value instanceof IComboBoxBasedMultiDimensionalParameter ? value.markerClass.name : null
        constraintName = value instanceof ConstrainedMultiDimensionalParameter ? value.constraints.name : null
        removeObsoleteParameters(value.valueRowCount, value.valueColumnCount)
        removeObsoleteTitles(value.rowCount, value.columnCount)
    }

    private void extractRowTitles(List titles, int offset) {
        for (int i = 0; i < titles.size(); i++) {
            modifyOrCreateParameterTitle(i + offset, 0, titles[i])
        }
    }

    private void extractColumnTitles(List titles, int offset) {
        for (int i = 0; i < titles.size(); i++) {
            modifyOrCreateParameterTitle(0, i + offset, titles[i])
        }
    }

    private void extractValues(List values) {
        List multiDimensionalParameterValuesAsList = new ArrayList()
        if (multiDimensionalParameterValues) {
            multiDimensionalParameterValuesAsList.clear()
            multiDimensionalParameterValuesAsList.addAll(multiDimensionalParameterValues)
        }
        if (values.any {it instanceof List}) {
            for (int i = 0; i < values.size(); i++) {
                for (int j = 0; j < values[i].size(); j++) {
                    modifyOrCreateParameterValue(j, i, values[i][j], multiDimensionalParameterValuesAsList)
                }
            }
        } else {
            for (int i = 0; i < values.size(); i++) {
                modifyOrCreateParameterValue(i, 0, values[i], multiDimensionalParameterValuesAsList)
            }
        }
    }

    private void removeObsoleteParameters(int rowCount, int columnCount) {
        def toRemove = multiDimensionalParameterValues.findAll { it.col >= columnCount || it.row >= rowCount }
        for (MultiDimensionalParameterValue value in toRemove) {
            removeFromMultiDimensionalParameterValues(value)
            value.delete()
        }
    }

    private void removeObsoleteTitles(int rowCount, int columnCount) {
        def toRemove = multiDimensionalParameterTitles.findAll { it.col >= columnCount || it.row >= rowCount }
        for (MultiDimensionalParameterTitle value in toRemove) {
            removeFromMultiDimensionalParameterTitles(value)
            value.delete()
        }
    }

    private void modifyOrCreateParameterValue(int row, int col, Object value, List values) {

        MultiDimensionalParameterValue parameterValue = null
        for (int i = 0; i < values.size() && parameterValue == null; i++) {
            def candidate = values[i]
            if (candidate.col == col && candidate.row == row) {
                parameterValue = candidate
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ObjectOutputStream stream = new ObjectOutputStream(byteArrayOutputStream)
        if (value instanceof BigDecimal) {
            value = value.doubleValue()
        }
        stream.writeObject(value)

        byte[] newValue = byteArrayOutputStream.toByteArray()
        if (parameterValue != null) {
            parameterValue.value = newValue
        } else {
            addToMultiDimensionalParameterValues(new MultiDimensionalParameterValue(col: col, row: row, value: newValue))
        }
    }

    private void modifyOrCreateParameterTitle(int row, int col, String value) {
        MultiDimensionalParameterTitle title = multiDimensionalParameterTitles.find {it.col == col && it.row == row }
        if (title != null) {
            title.title = value
        } else {
            addToMultiDimensionalParameterTitles(new MultiDimensionalParameterTitle(col: col, row: row, title: value))
        }
    }

    /**
     * This method should only be called on a persisted, non-dirty object, because the data is read directly from the DB
     * for performance reasons.
     */
    public Object getParameterInstance() {
        if (parameterObject == null) {
            Class clazz = Thread.currentThread().contextClassLoader.loadClass(className)
            Class markerClass
            if (markerClassName != null) {
                markerClass = Thread.currentThread().contextClassLoader.loadClass(markerClassName)
            }
            def mdpInstance = null
            switch (className) {
                case SimpleMultiDimensionalParameter.name:
                    mdpInstance = clazz.newInstance([getCellValues()] as Object[])
                    break;
                case TableMultiDimensionalParameter.name:
                    mdpInstance = clazz.newInstance([getCellValues(), getColumnTitles()] as Object[])
                    break;
                case MatrixMultiDimensionalParameter.name:
                    mdpInstance = clazz.newInstance([getCellValues(), getRowTitles(), getColumnTitles()] as Object[])
                    break;
                case ComboBoxMatrixMultiDimensionalParameter.name:
                    mdpInstance = clazz.newInstance([getCellValues(), getColumnTitles(), markerClass] as Object[])
                    break;
                case ComboBoxTableMultiDimensionalParameter.name:
                    mdpInstance = clazz.newInstance([getCellValues(), getColumnTitles(), markerClass] as Object[])
                    break;
                case ConstrainedMultiDimensionalParameter.name:
                    mdpInstance = clazz.newInstance([getCellValues(), getColumnTitles(), ConstraintsFactory.getConstraints(constraintName)] as Object[])
                    break;
            }
            parameterObject = mdpInstance
        }
        return parameterObject
    }

    private List getColumnTitles() {
        return multiDimensionalParameterTitles.findAll { it.row == 0 }.sort { it.col }.collect { it.title }
    }

    private List getRowTitles() {
        return multiDimensionalParameterTitles.findAll { it.col == 0 }.sort { it.row }.collect { it.title }
    }

    private List getCellValues() {
        List result = []
        Sql sql = new Sql(DataSourceUtils.getConnection(dataSource))
        int i = 0
        List column = sql.rows("SELECT value FROM multi_dimensional_parameter_value v where v.multi_dimensional_parameter_id = ? and v.col = ? order by v.row", [this.id, i])
        while (column.size() > 0) {
            result << column.collect {GroovyRowResult res ->
                ByteArrayInputStream str = new ByteArrayInputStream(res.getAt(0))
                ObjectInputStream str2 = new ObjectInputStream(str)
                return str2.readObject()
            }
            i++
            column = sql.rows("SELECT value FROM multi_dimensional_parameter_value v where v.multi_dimensional_parameter_id = ? and v.col = ? order by v.row", [this.id, i])
        }
        if (result.size() == 0) {
            return []
        }
        return result.size() > 1 ? result : result.get(0)
    }

    Class persistedClass() {
        MultiDimensionalParameter
    }

}
