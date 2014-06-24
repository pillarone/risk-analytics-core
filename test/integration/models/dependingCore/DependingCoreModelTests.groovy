package models.dependingCore
import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Before
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.*
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.engine.ModelTest
import org.pillarone.riskanalytics.core.simulation.engine.ResultData
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultDescriptor
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

class DependingCoreModelTests extends ModelTest {

    Simulation baseSimulation

    @Before
    void init() {
        FileImportService.importModelsIfNeeded(['Core'])
        Parameterization parameterization = new Parameterization("CoreParameters", CoreModel)
        parameterization.load()

        ResultConfiguration template = new ResultConfiguration("CoreResultConfiguration", CoreModel)
        template.load()

        baseSimulation = new Simulation("Result")
        baseSimulation.parameterization = parameterization
        baseSimulation.template = template
        baseSimulation.beginOfFirstPeriod = new DateTime()
        baseSimulation.start = new DateTime().minusMinutes(10)
        baseSimulation.end = new DateTime()
        baseSimulation.periodCount = 1
        baseSimulation.numberOfIterations = 1
        baseSimulation.modelClass = CoreModel
        baseSimulation.save()

        PathMapping path = PathMapping.findOrSaveByPathName("outPath")
        FieldMapping field = FieldMapping.findOrSaveByFieldName("x")
        FieldMapping field2 = FieldMapping.findOrSaveByFieldName("y")
        CollectorMapping collector = CollectorMapping.findOrSaveByCollectorName(AggregatedCollectingModeStrategy.IDENTIFIER)

        ResultWriter writer = new ResultWriter(baseSimulation.id)
        writeResult(new SingleValueResult(simulationRun: baseSimulation.simulationRun, period: 0, iteration: 1, path: path, field: field, collector: collector, valueIndex: 1, value: 100), writer)
        writeResult(new SingleValueResult(simulationRun: baseSimulation.simulationRun, period: 0, iteration: 1, path: path, field: field2, collector: collector, valueIndex: 1, value: 200), writer)
    }

    private void writeResult(SingleValueResult result, ResultWriter resultWriter) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeInt(1);
        dos.writeDouble(result.value);
        dos.writeDouble(0);
        dos.writeUTF("");

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.collector.id, result.period), null, bos.toByteArray(), 0));
    }

    Class getModelClass() {
        DependingCoreModel
    }

    @Override
    protected void prepareDataSource(ResultData dataSource) {
        dataSource.load(ParameterizationHelper.collectDataSourceDefinitions(run.parameterization.allParameterHolders), baseSimulation)
    }

    @Override
    List<ParameterHolder> getRuntimeParameters() {
        return [
                ParameterHolderFactory.getHolder("runtimeInt", 0, 10) ,
                ParameterHolderFactory.getHolder("runtimeSanityChecks", 0, true) ,
        ]
    }

}
