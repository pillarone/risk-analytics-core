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
//

    private void writeResult(SingleValueResult result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeInt(1);
        dos.writeDouble(result.value);

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.period), null, bos.toByteArray(), 0));
    }
}
