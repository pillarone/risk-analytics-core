package org.pillarone.riskanalytics.core.output

import models.core.CoreModel
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultDescriptor
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter

class CalculatorTests extends GroovyTestCase {

    SimulationRun run
    ResultWriter resultWriter

    void setUp() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['CoreParameters'])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['CoreConfiguration'])
        run = new SimulationRun()
        run.name = 'testRun'
        run.parameterization = ParameterizationDAO.findByName('CoreParameters')
        run.resultConfiguration = ResultConfigurationDAO.findByName('CoreConfiguration')
        run.model = CoreModel.name
        run.iterations = 3
        run.periodCount = 2
        run.modelVersionNumber = '1'
        assertNotNull run.save()
        resultWriter = new ResultWriter(run.id)

        PathMapping path1 = new PathMapping(pathName: "path1").save()
        PathMapping path2 = new PathMapping(pathName: "path2").save()
        PathMapping path3 = new PathMapping(pathName: "path3").save()
        PathMapping path4 = new PathMapping(pathName: "path4").save()
        CollectorMapping collector = new CollectorMapping(collectorName: AggregatedCollectingModeStrategy.IDENTIFIER).save()
        FieldMapping field = new FieldMapping(fieldName: "field").save()

        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 0, value: 1, path: path1, collector: collector, field: field)
        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 0, value: 2, path: path2, collector: collector, field: field)
        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 0, value: 3, path: path3, collector: collector, field: field)

        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 1, value: 4, path: path1, collector: collector, field: field)
        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 1, value: 5, path: path2, collector: collector, field: field)
        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 1, value: 6, path: path3, collector: collector, field: field)

        //period 0 should be ignored, because path does not contain a result for all iterations
        writeResult new SingleValueResult(simulationRun: run, iteration: 0, period: 1, value: 7, path: path4, collector: collector, field: field,)

    }

    void testResults() {

        int initialRecordCount = PostSimulationCalculation.count()

        Calculator calculator = new Calculator(run)
        calculator.calculate()

        assertEquals initialRecordCount + 14, PostSimulationCalculation.count()

        assertEquals 7, PostSimulationCalculation.countByRunAndKeyFigure(run, PostSimulationCalculation.MEAN)
        assertEquals 7, PostSimulationCalculation.countByRunAndKeyFigure(run, PostSimulationCalculation.IS_STOCHASTIC)

    }

    private void writeResult(SingleValueResult result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeDouble(result.value);

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.period), bos.toByteArray(), 0));
    }

}