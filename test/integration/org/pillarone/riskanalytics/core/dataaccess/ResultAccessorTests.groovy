package org.pillarone.riskanalytics.core.dataaccess

import org.junit.Before
import org.junit.Test
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

import static org.junit.Assert.*

class ResultAccessorTests {

    SimulationRun simulationRun
    PathMapping path1
    PathMapping path2
    FieldMapping field
    FieldMapping field2
    CollectorMapping collector
    CollectorMapping singleCollector

    private ResultWriter resultWriter

    @Before
    void setUp() {
        ResultAccessor.clearCaches()

        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])
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

    @Test
    void testGetAllResults() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 0)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 15)

        assertEquals(5, ResultAccessor.getAllResults(simulationRun).size())
    }

    @Test
    void testGetMean() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 3)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 4)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 5)

        assertEquals(3d, ResultAccessor.getMean(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName), 0)


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: 15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 4, value: 20)

        assertEquals(10d, ResultAccessor.getMean(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName), 0)

    }

    @Test
    void testGetMin() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 3)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 4)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 5)

        assertEquals(1, ResultAccessor.getMin(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName), 0)


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: 15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 4, value: 20)

        assertEquals(0, ResultAccessor.getMin(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName), 0)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 1, value: -5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 3, value: 15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 4, value: 20)

        assertEquals(-5, ResultAccessor.getMin(simulationRun, 1, path2.pathName, collector.collectorName, field.fieldName), 0)

    }

    @Test
    void testGetMax() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 1)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 2)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 3)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 4)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 4, value: 5)

        assertEquals(5, ResultAccessor.getMax(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName), 0)


        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 1, value: -5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 2, value: -10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 3, value: -15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 0, iteration: 4, value: -20)

        assertEquals(0, ResultAccessor.getMax(simulationRun, 0, path2.pathName, collector.collectorName, field.fieldName), 0)

        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 1, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 2, value: -10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 3, value: -15)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path2, field: field, collector: collector, period: 1, iteration: 4, value: -20)

        assertEquals(5, ResultAccessor.getMax(simulationRun, 1, path2.pathName, collector.collectorName, field.fieldName), 0)

    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testGetValuesSorted() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 1)

        List values = ResultAccessor.getValuesSorted(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName)
        assertEquals 5, values.size()
        assertEquals 0, values[0], 0
        assertEquals 1, values[1], 0
        assertEquals 5, values[2], 0
        assertEquals 10, values[3], 0
        assertEquals 20, values[4], 0

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

    @Test
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

    @Test
    void testIterationConstrainedValues() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 5)
        List<Integer> iterations = new ArrayList<Integer>()
        iterations.add(3)
        iterations.add(1)
        iterations.add(2)
        Map<Integer, Double> values = ResultAccessor.getIterationConstrainedValues(simulationRun, 0, path1.pathName, field.fieldName, collector.collectorName, iterations);
        values = values.sort { it.key }
        assertEquals 20, values.values().toList()[0], 0
        assertEquals 10, values.values().toList()[1], 0
        assertEquals 5, values.values().toList()[2], 0
    }

    @Test
    void testGetNthOrderStatisticSpare() {
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: 20)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 10)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 1, value: 8)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 2, value: 5)
        writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 3, value: 0)

        assertEquals "0%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 0.0), 0
        assertEquals "20%", 0d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 20.0), 0
        assertEquals "25%", 2.5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 25.0), 0
        assertEquals "39%", 2.5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 39.0), 0
        assertEquals "40%", 5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 40.0), 0
        assertEquals "50%", 6.5d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 50.0), 0
        assertEquals "60%", 8d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 60.0), 0
        assertEquals "61%", 9d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 61.0), 0
        assertEquals "71%", 9d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 71.0), 0
        assertEquals "80%", 10d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 80.0), 0
        assertEquals "100%", 20d, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, 100.0), 0
    }

    @Test
    void testGetNthOrderStatistic() {
        simulationRun.iterations = 100
        (1..100).each {
            writeResult new SingleValueResult(simulationRun: simulationRun, valueIndex: 0, path: path1, field: field, collector: collector, period: 0, iteration: 0, value: it)
        }

        (1..100).each {
            assertEquals "equals $it%", it, ResultAccessor.getNthOrderStatistic(simulationRun, 0, path1.pathName, collector.collectorName, field.fieldName, it), 0
        }
    }
}
