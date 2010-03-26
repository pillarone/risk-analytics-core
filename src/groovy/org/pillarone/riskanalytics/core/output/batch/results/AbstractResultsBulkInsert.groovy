package org.pillarone.riskanalytics.core.output.batch.results

import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert
import org.pillarone.riskanalytics.core.output.SingleValueResult

import org.codehaus.groovy.grails.commons.ApplicationHolder


abstract class AbstractResultsBulkInsert extends AbstractBulkInsert {

    void addResults(List results) {
        List values = []
        for (SingleValueResult result in results) {
            values << result.simulationRun.id
            values << result.period
            values << result.iteration
            values << result.path.id
            values << result.field.id
            values << result.collector.id
            values << result.value
            writeResult(values)
            values.clear()
        }
        writer.flush()
    }

    public static AbstractBulkInsert getBulkInsertInstance() {
        Class bulkClass = ApplicationHolder.application?.config?.resultBulkInsert
        if (!bulkClass) {
            return new GenericBulkInsert()
        }
        return AbstractBulkInsert.classLoader.loadClass(bulkClass.name).newInstance()
    }
}
