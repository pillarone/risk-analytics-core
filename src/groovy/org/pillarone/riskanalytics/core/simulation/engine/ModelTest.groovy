package org.pillarone.riskanalytics.core.simulation.engine

import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.FileOutput
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.batch.OutputStrategyFactory
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock
import org.pillarone.riskanalytics.core.simulation.item.*
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.util.MathUtils

import static org.junit.Assert.*

/**
 * An abstract class which provides functionality to run model tests.
 * This class does not belong to the test sources so that it can be used in plugins too.
 */
abstract class ModelTest {

    private static final Log LOG = LogFactory.getLog(ModelTest)

    String refFileName
    String newFileName
    private static final EPSILON = 1E-6

    protected SimulationRunner runner

    String getParameterFileName() {
        (getModelClass().simpleName - "Model") + "Parameters"
    }

    String getResultConfigurationFileName() {
        (getModelClass().simpleName - "Model") + "ResultConfiguration"
    }

    String getStructureFileName() {
        (getModelClass().simpleName - "Model") + "Structure"
    }

    String getResultFileName() {
        return "${modelClass.simpleName}_${parameterFileName}_${resultConfigurationFileName}"
    }

    private String getPath() {
        return "test/data/refResults/" + getFolderName() + "/"
    }

    String getFolderName() {
        def name = modelClass.simpleName - "Model"
        return name.replaceFirst(name.getAt(0), name.getAt(0).toLowerCase())
    }

    String getParameterDisplayName() {
        getParameterFileName()
    }

    String getResultConfigurationDisplayName() {
        getResultConfigurationFileName()
    }

    abstract Class getModelClass()

    int getPeriodCount() {
        1
    }

    int getIterationCount() {
        10
    }

    List<ParameterHolder> getRuntimeParameters() {
        []
    }

    Simulation run

    SimulationProfile simulationProfile

    @Before
    void setUp() {
        ParameterizationDAO.withNewSession { def session ->
            MappingCache.instance.clear()
            MathUtils.initRandomStreamBase(1234)

            new ParameterizationImportService().compareFilesAndWriteToDB([getParameterFileName()])
            new ResultConfigurationImportService().compareFilesAndWriteToDB([getResultConfigurationFileName()])
            new ModelStructureImportService().compareFilesAndWriteToDB([getStructureFileName()])

            def parameter = ParameterizationDAO.findByName(getParameterDisplayName())
            assertNotNull parameter

            Parameterization parameterization = new Parameterization(parameter.name)
            parameterization.modelClass = getModelClass()
            parameterization.load()

            def resultConfig = ResultConfigurationDAO.findByName(getResultConfigurationDisplayName())
            assertNotNull resultConfig

            ResultConfiguration resultConfiguration = new ResultConfiguration(resultConfig.name, getModelClass())
            resultConfiguration.load()

            Class modelClass = getModelClass()
            def modelInstance = modelClass.newInstance() as Model

            run = new Simulation(getResultFileName())
            run.parameterization = parameterization
            run.template = resultConfiguration
            run.modelClass = modelClass
            run.modelVersionNumber = new VersionNumber("1")
            run.periodCount = getPeriodCount()
            run.numberOfIterations = getIterationCount()
            run.structure = ModelStructure.getStructureForModel(modelClass)
            if (modelInstance.requiresStartDate()) {
                run.beginOfFirstPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
            }

            for (ParameterHolder parameterHolder in getRuntimeParameters()) {
                run.addParameter(parameterHolder)
            }

            assertNotNull run.save()
            refFileName = getPath() + getResultFileName() + "_ref.tsl"
            newFileName = getPath() + getResultFileName() + ".tsl"
            clean()
            session.flush()
        }
        simulationProfile = new SimulationProfile('testProfile', run.modelClass)
        simulationProfile.numberOfIterations = run.numberOfIterations
        simulationProfile.randomSeed = run.randomSeed
        simulationProfile.runtimeParameters = run.runtimeParameters
        simulationProfile.template = run.template
        assert simulationProfile.save()

    }

    @Test
    final void testModelRun() {
        runner = SimulationRunner.createRunner()
        ICollectorOutputStrategy output = outputStrategy
        SimulationConfiguration configuration = new SimulationConfiguration(run, output)
        configuration.resultDataSource = new ResultDataSource()
        prepareDataSource(configuration.resultDataSource)
        configuration.simulationBlocks = [new SimulationBlock(0, iterationCount, 0)]
        configuration.createMappingCache(run.template)
        runner.simulationConfiguration = configuration


        runner.start()

        assertNull "${runner.error?.error?.message}", runner.error
        if (shouldCompareResults()) {
            compareResults()
        }
        postSimulationEvaluation()
    }

    protected void prepareDataSource(ResultDataSource dataSource) {

    }

    @After
    void cleanUp() {
        SimulationRun.withNewSession { def session ->
            Parameterization parameterization = run.parameterization
            ResultConfiguration template = run.template
            run.delete()
            parameterization.delete()
            template.delete()
            simulationProfile.delete()
            session.flush()
        }
    }

    protected ICollectorOutputStrategy getOutputStrategy() {
        FileOutput output = new FileOutput()
        output.resultLocation = getPath()
        output
    }

    public void postSimulationEvaluation() {}

    /** will fail if returning true during the first run as the *_ref.tsl needs to be created */
    protected boolean shouldCompareResults() {
        false
    }

    /** Overwrite and return false in order to log all differences */
    protected boolean stopAtFirstDifference() {
        true
    }

    private void clean() {
        File resultFile = new File(newFileName)
        if (resultFile.exists()) {
            if (!resultFile.delete())
                LOG.info "deleting file failed: ${resultFile.name}"
        }
    }

    private void compareResults() {
        File refFile = new File(refFileName)
        if (!refFile.exists())
            fail("No referenceResultFileName defined for ${modelClass.name}: ($refFileName)")
        FileInputStream referenceFis = new FileInputStream(refFile)

        FileInputStream resultFis = new FileInputStream(new File(newFileName))
        compare(resultFis, referenceFis)

    }

    /**
     * compares two inputStreams and return true if the inputStreams have the same content.
     *
     */
    public void compare(InputStream result, InputStream reference) throws IOException {
        int in1byte, in2byte;

        final int byteBufferSize = 10448;

        reference = new BufferedInputStream(reference, byteBufferSize);
        result = new BufferedInputStream(result, byteBufferSize);

        Map referenceResults = [:]

        List lines = reference.readLines()
        for (int i = 1; i < lines.size(); i++) {
            String line = lines[i]
            String[] info = line.split()
            int iteration = Integer.parseInt(info[0])
            int period = Integer.parseInt(info[1])
            int valueIndex = Integer.parseInt(info[2])
            String path = info[3]
            String field = info[4]
            double value = Double.parseDouble(info[5])
            referenceResults.put(createKey(iteration, period, valueIndex, path, field), value)
        }

        List resultLines = result.readLines()

        int countDifferences = 0
        for (int i = 1; i < resultLines.size(); i++) {
            String line = resultLines[i]
//            String[] info = line.split("\t")
            String[] info = line.split()
            int iteration = Integer.parseInt(info[0])
            int period = Integer.parseInt(info[1])
            int valueIndex = Integer.parseInt(info[2])
            String path = info[3]
            String field = info[4]
            double value = Double.parseDouble(info[5])
            int key = createKey(iteration, period, valueIndex, path, field)
            def expectedResult = referenceResults.remove(key)
            assertNotNull "No result found for I$iteration P$period $path $field", expectedResult
            if (stopAtFirstDifference()) {
                assertEquals("Different value at I$iteration P$period $path $field", expectedResult, value, EPSILON)
            } else if (Math.abs(expectedResult - value) > EPSILON) {
                LOG.error("Different value at I$iteration P$period $path $field, expected $expectedResult, $value")
                countDifferences++
            }

        }
        assertEquals "Differences found", 0, countDifferences
        assertEquals "Different result count: Reference: ${lines.size()}, Actual: ${resultLines.size()}", lines.size(), resultLines.size()
        assertTrue("${referenceResults.size()} more results than expected", referenceResults.size() == 0)

    }

    private int createKey(int iteration, int period, int valueIndex, String path, String field) {
        HashCodeBuilder builder = new HashCodeBuilder()
        builder.append(iteration).append(period).append(valueIndex).append(path).append(field)
        return builder.toHashCode()
    }
}