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
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy

class ResultAccessorTests extends GroovyTestCase {

    SimulationRun simulationRun
    PathMapping path1
    PathMapping path2
    FieldMapping field
    FieldMapping field2
    CollectorMapping collector
    CollectorMapping singleCollector

    private ResultWriter resultWriter

    void setUp() {
        ResultAccessor.clearCaches()

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

        singleCollector = CollectorMapping.findByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)
        if (singleCollector == null) {
            singleCollector = new CollectorMapping(collectorName: SingleValueCollectingModeStrategy.IDENTIFIER).save()
        }
    }

    void testGetAllResults() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 15)

        assertEquals(5, ResultAccessor.getAllResults(simulationRun).size())
    }

    void testGetMean() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 3)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 4)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 5)

        assertEquals(3d, ResultAccessor.getMean(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName))


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: 15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 4, value: 20)

        assertEquals(10d, ResultAccessor.getMean(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName))

    }

    void testGetMin() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 3)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 4)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 5)

        assertEquals(1, ResultAccessor.getMin(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName))


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: 15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 4, value: 20)

        assertEquals(0, ResultAccessor.getMin(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName))

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 1, value: -5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 3, value: 15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 4, value: 20)

        assertEquals(-5, ResultAccessor.getMin(simulationRun, 1, path2.pathName, collector.collectorName, field.fieldName))

    }

    void testGetMax() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 3)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 4)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 5)

        assertEquals(5, ResultAccessor.getMax(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName))


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: -5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: -10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: -15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 4, value: -20)

        assertEquals(0, ResultAccessor.getMax(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName))

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 2, value: -10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 3, value: -15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 4, value: -20)

        assertEquals(5, ResultAccessor.getMax(simulationRun, 1, path2.pathName, collector.collectorName, field.fieldName))

    }

    void testGetDistinctPaths() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 15)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 1, value: 15)


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: singleCollector, period: 1, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: singleCollector, period: 1, iteration: 1, value: 15)

        List<ResultPathDescriptor> paths = ResultAccessor.getDistinctPaths(simulationRun).sort()
        assertEquals 3, paths.size()
    }

    void testHasDifferentValues() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 10)

        assertFalse ResultAccessor.hasDifferentValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: 20)

        assertTrue ResultAccessor.hasDifferentValues(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 3, value: 10)

        assertTrue ResultAccessor.hasDifferentValues(simulationRun, 1, path1.pathName, collector.collectorName, field.fieldName)

    }

    void testGetValues() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 20)

        List values = ResultAccessor.getValues(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 5, values.size()
        assertTrue values.contains(0d)
        assertTrue values.contains(1d)
        assertTrue values.contains(5d)
        assertTrue values.contains(10d)
        assertTrue values.contains(20d)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 2, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 3, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 1, iteration: 4, value: 20)

        values = ResultAccessor.getValues(simulationRun, 1, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 5, values.size()
        assertTrue values.contains(1d)
        assertTrue values.contains(2d)
        assertTrue values.contains(5d)
        assertTrue values.contains(10d)
        assertTrue values.contains(20d)

    }

    void testGetValuesSorted() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 1)

        List values = ResultAccessor.getValuesSorted(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 5, values.size()
        assertEquals 0, values[0]
        assertEquals 1, values[1]
        assertEquals 5, values[2]
        assertEquals 10, values[3]
        assertEquals 20, values[4]

    }



    private void writeResult(SingleValueResult result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeInt(1);
        dos.writeDouble(result.value);
        dos.writeDouble(0);

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.collector.id, result.period), null, bos.toByteArray(), 0));
    }

    void testGetConstrainedIteration() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 5)

        List values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, ">", 10);
        assertEquals 1, values[0]

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, "<", 20);
        assertEquals 2, values.size()

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, ">=", 0);
        assertEquals 3, values.size()

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, "<=", 10);
        assertEquals 2, values.size()

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, "<", 10);
        assertEquals 3, values[0]

        values = ResultAccessor.getCriteriaConstrainedIterations(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, "=", 10);
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
        List values = ResultAccessor.getIterationConstrainedValues(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, iterations);

        assertEquals 20, values[0]
        assertEquals 10, values[1]
        assertEquals 5, values[2]
    }

    // todo(sku): migration needed to work on kti branch
    void testGetNthOrderStatisticSpare() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 8)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 0)

        assertEquals "0%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.LESS_THAN)
        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.LESS_EQUALS)
        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.EQUALS)
        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0, CompareOperator.GREATER_THAN)
        assertEquals "20%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.LESS_THAN)
        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0, CompareOperator.LESS_EQUALS)
        assertEquals "25%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 25.0, CompareOperator.LESS_EQUALS)
        assertEquals "39%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 39.0, CompareOperator.LESS_EQUALS)
        assertEquals "40%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 40.0, CompareOperator.LESS_EQUALS)
        assertEquals "50%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 50.0, CompareOperator.LESS_EQUALS)
        assertEquals "60%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 60.0, CompareOperator.LESS_EQUALS)
        assertEquals "80%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 80.0, CompareOperator.LESS_EQUALS)
        assertEquals "100%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.LESS_THAN)
        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0, CompareOperator.EQUALS)
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
            writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: it)
        }

//        exact match --> < lower than <=, > greater than >=
        (1..100).each {
            assertEquals "less equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_EQUALS)
            assertEquals "equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.EQUALS)
            assertEquals "greater equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_EQUALS)
            if (it == 1) {
                assertEquals "less $it%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_THAN)
            }
            else {
                assertEquals "less $it%", it - 1, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.LESS_THAN)
            }
            if (it == 100) {
                assertEquals "greater $it%", null, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_THAN)
            }
            else {
                assertEquals "greater $it%", it + 1, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it, CompareOperator.GREATER_THAN)
            }
        }

//        mismatch --> < equal to <=, > equal to >=
        (1..99).each {
            double lessEqual = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.LESS_EQUALS)
            double lessThan = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.LESS_THAN)
            double greaterEqual = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.GREATER_EQUALS)
            double greaterThan = ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it + 0.5, CompareOperator.GREATER_THAN)
            assertEquals "less ${it + 0.5}%: $lessEqual == $lessThan", lessEqual, lessThan
            assertEquals "greater ${it - 0.5}%: $greaterEqual == $greaterThan", greaterEqual, greaterThan
            assertTrue "different ${it + 0.5}%: $lessEqual < $greaterEqual", lessEqual < greaterEqual
        }
    }
}
