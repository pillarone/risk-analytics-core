package org.pillarone.riskanalytics.core.output.batch.calculations

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert

abstract class AbstractCalculationsBulkInsert extends AbstractBulkInsert {

    void addResults(int period, String keyFigure, BigDecimal keyFigureParameter, long path, long field, long collector, Double value) {
        List values = []
        values << simulationRun.id
        values << period
        values << path
        values << field
        values << collector
        values << keyFigure
        values << (keyFigureParameter != null ? keyFigureParameter : getNull())
        values << value
        writeResult(values)
        values.clear()
        writer.flush()
    }

    protected abstract String getNull()

    public static AbstractCalculationsBulkInsert getBulkInsertInstance() {
        Class bulkClass = ApplicationHolder.application?.config?.calculationBulkInsert
        if (!bulkClass) {
            return new GenericBulkInsert()
        }
        return AbstractCalculationsBulkInsert.classLoader.loadClass(bulkClass.name).newInstance()
    }

}
