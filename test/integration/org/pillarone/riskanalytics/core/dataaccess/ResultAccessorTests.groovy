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
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultDescriptor

class ResultAccessorTests extends GroovyTestCase {

    SimulationRun simulationRun
    PathMapping path1
    PathMapping path2
    FieldMapping field
    FieldMapping field2
    CollectorMapping collector

    private ResultWriter resultWriter

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
        resultWriter = new ResultWriter(simulationRun.id)

        path1 = PathMapping.findByPathName('testPath1')
        if (path1 == null) {
            path1 = new PathMapping(pathName: 'testPath1').save()
        }

        path2 = PathMapping.findByPathName('testPath2')
        if (path2 == null) {
            path2 = new PathMapping(pathName: 'testPath2').save()
        }

        field = FieldMapping.findByFieldName('ultimate')
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

    protected void tearDown() {
        resultWriter.close()
    }

    void testAvgIsStochastic() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 10)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 15)

        List<Object[]> results = ResultAccessor.getAvgAndIsStochasticForSimulationRun(simulationRun)
        assertEquals 2, results.size()

        Object[] result = results.find { it[0] == path1.id }

        assertEquals path1.id, result[0]
        assertEquals 0, result[1]
//        assertEquals collector.id, result[2]
        assertEquals field.id, result[3]
        assertEquals 5, result[4]
        assertEquals 0, result[5]
        assertEquals 10, result[6]

        result = results.find { it[0] == path2.id }

        assertEquals path2.id, result[0]
        assertEquals 0, result[1]
//        assertEquals collector.id, result[2]
        assertEquals field.id, result[3]
        assertEquals 10, result[4]
        assertEquals 5, result[5]
        assertEquals 15, result[6]

    }

    void testGetPaths() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 15)

        List paths = ResultAccessor.getPaths(simulationRun).sort()
        assertEquals 2, paths.size()
        assertEquals path1.pathName, paths[0]
        assertEquals path2.pathName, paths[1]
    }

    void testGetMeanMinMax() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20)

        double mean = ResultAccessor.getMean(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 10, mean

        double min = ResultAccessor.getMin(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 0, min

        double max = ResultAccessor.getMax(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 20, max
    }

    void testHasDifferentValues() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)

        assertFalse ResultAccessor.hasDifferentValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20)

        assertTrue ResultAccessor.hasDifferentValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)

    }

    void testGetValues() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20)

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
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 0)

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



    private void writeResult(SingleValueResult result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeInt(1);
        dos.writeDouble(result.value);
        dos.writeDouble(0);

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.period), null, bos.toByteArray(), 0));
    }

    void testGetConstrainedIteration() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 5)

        List values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, ">", 10);
        assertEquals 1, values[0]

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, "<", 20);
        assertEquals 2, values.size()

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, ">=", 0);
        assertEquals 3, values.size()

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, "<=", 10);
        assertEquals 2, values.size()

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, "<", 10);
        assertEquals 3, values[0]

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, "=", 10);
        assertEquals 2, values[0]
    }

    void testIterationConstrainedValues() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 5)
        List<Integer> iterations = new ArrayList<Integer>()
        iterations.add(3)
        iterations.add(1)
        iterations.add(2)
        List values = ResultAccessor.getIterationConstrainedValues(simulationRun, 0, path1.pathName, field.fieldName, iterations);

        assertEquals 20, values[0]
        assertEquals 10, values[1]
        assertEquals 5, values[2]
    }

    // todo(sku): migration needed to work on kti branch
//    void testGetNthOrderStatisticSpare() {
//        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20).save()
//        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10).save()
//        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 8).save()
//        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5).save()
//        assertNotNull new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 0).save()
//
//        assertEquals "0%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.LESS_THAN)
//        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.LESS_EQUALS)
//        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.EQUALS)
//        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.GREATER_THAN)
//        assertEquals "20%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.LESS_THAN)
//        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.LESS_EQUALS)
//        assertEquals "25%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 25.0, CompareOperator.LESS_EQUALS)
//        assertEquals "39%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 39.0, CompareOperator.LESS_EQUALS)
//        assertEquals "40%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 40.0, CompareOperator.LESS_EQUALS)
//        assertEquals "50%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 50.0, CompareOperator.LESS_EQUALS)
//        assertEquals "60%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 60.0, CompareOperator.LESS_EQUALS)
//        assertEquals "80%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 80.0, CompareOperator.LESS_EQUALS)
//        assertEquals "100%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.LESS_THAN)
//        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.EQUALS)
//        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.LESS_EQUALS)
//
//        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "20%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.GREATER_THAN)
//        assertEquals "25%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 25.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "39%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 39.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "40%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 40.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "50%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 50.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "60%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 60.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "80%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 80.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "99%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 99.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.GREATER_EQUALS)
//        assertEquals "100%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.GREATER_THAN)
//    }
//
//    void testGetNthOrderStatistic() {
//        simulationRun.iterations = 100
//        (1..100).each {
//            new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: it).save()
//        }
//
//        exact match --> < lower than <=, > greater than >=
//        (1..100).each {
//            assertEquals "less equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_EQUALS)
//            assertEquals "equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.EQUALS)
//            assertEquals "greater equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_EQUALS)
//            if (it == 1) {
//                assertEquals "less $it%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_THAN)
//            }
//            else {
//                assertEquals "less $it%", it - 1, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_THAN)
//            }
//            if (it == 100) {
//                assertEquals "greater $it%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_THAN)
//            }
//            else {
//                assertEquals "greater $it%", it + 1, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_THAN)
//            }
//        }
//
//        mismatch --> < equal to <=, > equal to >=
//        (1..99).each {
//            double lessEqual = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.LESS_EQUALS)
//            double lessThan = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.LESS_THAN)
//            double greaterEqual = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.GREATER_EQUALS)
//            double greaterThan = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.GREATER_THAN)
//            assertEquals "less ${it + 0.5}%: $lessEqual == $lessThan", lessEqual, lessThan
//            assertEquals "greater ${it - 0.5}%: $greaterEqual == $greaterThan", greaterEqual, greaterThan
//            assertTrue "different ${it + 0.5}%: $lessEqual < $greaterEqual", lessEqual < greaterEqual
//        }
//    }
}
