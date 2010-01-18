package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class DBCleanUpServiceTests extends GroovyTestCase {
    private DBCleanUpService service

    void testCleanUp() {
        int runCount = SimulationRun.count()
        int resultCount = SingleValueResult.count()
        SimulationRun simulationRun = new SimulationRun(name: "testRun", model: "model", startTime: new Date(), modelVersionNumber: new VersionNumber("1").toString())
        ParameterizationDAO params = new ParameterizationDAO()
        params.name = "name"
        params.modelClassName = "model"
        params.itemVersion = "0.0"
        params.periodCount = 1
        simulationRun.parameterization = params.save()

        ResultConfigurationDAO template = new ResultConfigurationDAO()
        template.name = "name"
        template.modelClassName = "model"
        template.itemVersion = "0.0"
        simulationRun.resultConfiguration = template.save()

        PathMapping path = new PathMapping(pathName: "path").save()
        CollectorMapping collector = new CollectorMapping(collectorName: "collector").save()
        FieldMapping field = new FieldMapping(fieldName: "field").save()

        if (!simulationRun.save()) {
            simulationRun.errors.each {
                println it
            }
        }
        10.times {
            SingleValueResult result = new SingleValueResult(simulationRun: simulationRun, path: path, collector: collector, field: field, iteration: it, period: it, value: 0d)
            if (!result.save(flush: true)) {
                result.errors.each {
                    println it
                }
            }
        }

        assertEquals runCount + 1, SimulationRun.count()
        assertEquals resultCount + 10, SingleValueResult.count()

        new DBCleanUpService().cleanUp()

        assertEquals runCount, SimulationRun.count()
        assertEquals resultCount, SingleValueResult.count()

    }
}
