package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import models.core.CoreModel
import org.pillarone.riskanalytics.core.output.SingleValueResult


class ResultAccessorTests extends GroovyTestCase {

    SimulationRun simulationRun
    PathMapping path1
    PathMapping path2
    FieldMapping field
    FieldMapping field2
    CollectorMapping collector

    void setUp() {

        new ParameterizationImportService().compareFilesAndWriteToDB(['CoreParameters'])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['CoreResultConfiguration'])
        simulationRun = new SimulationRun()
        simulationRun.name = "testRun"
        simulationRun.parameterization = ParameterizationDAO.findByName('CoreParameters')
        simulationRun.resultConfiguration = ResultConfigurationDAO.findByName('CoreResultConfiguration')
        simulationRun.model = CoreModel.name
        simulationRun.periodCount = 2
        simulationRun.iterations = 5
        simulationRun.randomSeed = 0
        simulationRun.modelVersionNumber = "1"

        simulationRun = simulationRun.save(flush: true)

        path1 = PathMapping.findByPathName('testPath1')
        if (path1 == null) {
            path1 = new PathMapping(pathName: 'testPath1').save()
        }

        path2 = PathMapping.findByPathName('testPath2')
        if (path2 == null) {
            path2 = new PathMapping(pathName: 'testPath2').save()
        }

        field = FieldMapping.findByFieldName('Ultimate')
        if (field == null) {
            field = new FieldMapping(fieldName: 'ultimate').save()
        }

        field2 = FieldMapping.findByFieldName('value')
        if (field2 == null) {
            field2 = new FieldMapping(fieldName: 'value').save()
        }

        collector = CollectorMapping.findByCollectorName('collector')
        if (collector == null) {
            collector = new CollectorMapping(collectorName: 'collector').save()
        }
    }

    void testAvgIsStochastic() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 10).save()

        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 5).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 15).save()

        List<Object[]> results = ResultAccessor.getAvgAndIsStochasticForSimulationRun(simulationRun)
        assertEquals 2, results.size()

        Object[] result = results[0]

        assertEquals path1.id, result[0]
        assertEquals 0, result[1]
        assertEquals collector.id, result[2]
        assertEquals field.id, result[3]
        assertEquals 5, result[4]
        assertEquals 0, result[5]
        assertEquals 10, result[6]

        result = results[1]

        assertEquals path2.id, result[0]
        assertEquals 0, result[1]
        assertEquals collector.id, result[2]
        assertEquals field.id, result[3]
        assertEquals 10, result[4]
        assertEquals 5, result[5]
        assertEquals 15, result[6]

    }
}
