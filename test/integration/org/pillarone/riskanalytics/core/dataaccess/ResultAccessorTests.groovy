package org.pillarone.riskanalytics.core.dataaccess

import models.core.CoreModel
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.output.*

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

        List<Object[]> results = ResultAccessor.getAvgAndIsStochasticForSimulationRun(simulationRun, -1)
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

    void testGetPaths() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10).save()

        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 5).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 15).save()

        List paths = ResultAccessor.getPaths(simulationRun).sort()
        assertEquals 2, paths.size()
        assertEquals path1.pathName, paths[0]
        assertEquals path2.pathName, paths[1]
    }

    void testGetMeanMinMax() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20).save()

        double mean = ResultAccessor.getMean(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 10, mean

        double min = ResultAccessor.getMin(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 0, min

        double max = ResultAccessor.getMax(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 20, max
    }

    void testHasDifferentValues() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10).save()

        assertFalse ResultAccessor.hasDifferentValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)

        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20).save()

        assertTrue ResultAccessor.hasDifferentValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)

    }

    void testGetValues() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 5).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20).save()

        List values = ResultAccessor.getValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 4, values.size()
        assertTrue values.contains(0d)
        assertTrue values.contains(5d)
        assertTrue values.contains(10d)
        assertTrue values.contains(20d)

        values = ResultAccessor.getValues(simulationRun, 0, path1.id, collector.id, field.id)
        assertEquals 4, values.size()
        assertTrue values.contains(0d)
        assertTrue values.contains(5d)
        assertTrue values.contains(10d)
        assertTrue values.contains(20d)
    }

    void testGetValuesSorted() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 0).save()

        List values = ResultAccessor.getValuesSorted(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 4, values.size()
        assertEquals 0, values[0]
        assertEquals 5, values[1]
        assertEquals 10, values[2]
        assertEquals 20, values[3]

        values = ResultAccessor.getValuesSorted(simulationRun, 0, path1.id, collector.id, field.id)
        assertEquals 4, values.size()
        assertEquals 0, values[0]
        assertEquals 5, values[1]
        assertEquals 10, values[2]
        assertEquals 20, values[3]
    }

    void testGetNthOrderStatisticSpare() {
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 8).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5).save()
        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 0).save()

        assertEquals "0%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.LESS_THAN)
        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.LESS_EQUALS)
        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.LESS_THAN)
        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.LESS_EQUALS)
        assertEquals "25%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 25.0, CompareOperator.LESS_EQUALS)
        assertEquals "39%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 39.0, CompareOperator.LESS_EQUALS)
        assertEquals "40%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 40.0, CompareOperator.LESS_EQUALS)
        assertEquals "50%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 50.0, CompareOperator.LESS_EQUALS)
        assertEquals "60%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 60.0, CompareOperator.LESS_EQUALS)
        assertEquals "80%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 80.0, CompareOperator.LESS_EQUALS)
        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.LESS_EQUALS)

        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.GREATER_EQUALS)
        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.GREATER_EQUALS)
        assertEquals "20%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.GREATER_THAN)
        assertEquals "25%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 25.0, CompareOperator.GREATER_EQUALS)
        assertEquals "39%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 39.0, CompareOperator.GREATER_EQUALS)
        assertEquals "40%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 40.0, CompareOperator.GREATER_EQUALS)
        assertEquals "50%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 50.0, CompareOperator.GREATER_EQUALS)
        assertEquals "60%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 60.0, CompareOperator.GREATER_EQUALS)
        assertEquals "80%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 80.0, CompareOperator.GREATER_EQUALS)
        assertEquals "99%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 99.0, CompareOperator.GREATER_EQUALS)
        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.GREATER_EQUALS)
        assertEquals "100%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.GREATER_THAN)
    }

    void testGetNthOrderStatistic() {
        simulationRun.iterations = 100
        (1..100).each {
            new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: it).save()
        }

        (1..100).each {
            assertEquals "less equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_EQUALS)
            assertEquals "equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.EQUALS)
            assertEquals "greater equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_EQUALS)
        }
    }
}
