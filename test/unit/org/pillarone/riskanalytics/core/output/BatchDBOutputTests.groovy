package org.pillarone.riskanalytics.core.output

import grails.test.GrailsUnitTestCase
import org.pillarone.riskanalytics.core.output.batch.results.AbstractResultsBulkInsert

class BatchDBOutputTests extends GrailsUnitTestCase {

    void testLeftShift() {
        mockDomain SimulationRun
        mockDomain PathMapping
        mockDomain FieldMapping
        mockDomain CollectorMapping


        DBOutput batchDBOutput = new DBOutput(batchInsert: new LoggingBulkInsert())
        SingleValueResultPOJO result = new SingleValueResultPOJO()
        result.simulationRun = new SimulationRun(name: "BatchDBOuptuMtTests", id: 1)
        result.period = 1
        result.iteration = 1
        result.pathId = 2
        result.fieldId = 3
        result.collectorId = 4
        result.value = 1.3d

        batchDBOutput << [result]
    }
}

public class LoggingBulkInsert extends AbstractResultsBulkInsert {

    protected void writeResult(List values) {
        LOG.debug values
    }

    protected void save() {
        LOG.debug "save called"
    }

}