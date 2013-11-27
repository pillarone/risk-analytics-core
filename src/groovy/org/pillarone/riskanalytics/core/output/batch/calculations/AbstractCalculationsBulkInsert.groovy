package org.pillarone.riskanalytics.core.output.batch.calculations

import grails.util.Holders
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert

abstract class AbstractCalculationsBulkInsert extends AbstractBulkInsert {

    @CompileStatic
    void addResults(int period, String keyFigure, Double keyFigureParameter, long path, long field, long collector, Double value) {
        List values = []
        values << simulationRunId
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
        Class bulkClass = Holders?.config?.calculationBulkInsert
        if (!bulkClass) {
            return new GenericBulkInsert()
        }
        return Thread.currentThread().contextClassLoader.loadClass(bulkClass.name).newInstance()
    }

}
