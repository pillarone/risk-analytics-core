package org.pillarone.riskanalytics.core.simulation.engine

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.FileOutput
import org.gridgain.grid.Grid
import org.gridgain.grid.GridMessageListener
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationJob


abstract class GridModelTest extends GroovyTestCase {

    private static final Log LOG = LogFactory.getLog(ModelTest)
    String refFileName
    String newFileName

    Grid grid

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

    Simulation run

    protected void setUp() {
        super.setUp()
        MathUtils.initRandomStreamBase(1234)

        new ParameterizationImportService().compareFilesAndWriteToDB([getParameterFileName()])
        new ResultConfigurationImportService().compareFilesAndWriteToDB([getResultConfigurationFileName()])
        new ModelStructureImportService().compareFilesAndWriteToDB([getStructureFileName()])

        def parameter = ParameterizationDAO.findByName(getParameterDisplayName())
        assertNotNull parameter

        Parameterization parameterization = new Parameterization(parameter.name)
        parameterization.load()

        def resultConfig = ResultConfigurationDAO.findByName(getResultConfigurationDisplayName())
        assertNotNull resultConfig

        ResultConfiguration resultConfiguration = new ResultConfiguration(resultConfig.name)
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

        if (modelInstance.requiresStartDate()) {
            run.beginOfFirstPeriod = new DateTime(2009, 1, 1, 0, 0, 0, 0)
        }

        assertNotNull run.save()
        refFileName = getPath() + getResultFileName() + "_ref.tsl"
        newFileName = getPath() + getResultFileName() + ".tsl"
        clean()
    }

    final void testGridModelRun() {

        SimulationConfiguration configuration = new SimulationConfiguration(simulation: run, outputStrategy: getOutputStrategy())
        RunSimulationService runSimulationService = RunSimulationService.getService()

        def result = runSimulationService.runSimulationOnGrid(configuration)

        LOG.info result.toString()

//        assertNull "${runner.error?.error?.message}", runner.error
        if (shouldCompareResults()) {
            compareResults()
        }
        postSimulationEvaluation()
    }

    protected ICollectorOutputStrategy getOutputStrategy() {
        FileOutput output = new FileOutput()
        output.resultLocation = getPath()
        output
    }

    public void postSimulationEvaluation() {}

    protected boolean shouldCompareResults() {
        false
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
            fail("No referenceResultFileName defined for ${modelClass.name}Model.")
        FileInputStream referenceFis = new FileInputStream(refFile)

        FileInputStream resultFis = new FileInputStream(new File(newFileName))
        if (!compare(resultFis, referenceFis)) {
            List resultBytes = new File(newFileName).readBytes().toList()
            List refBytes = new File(refFileName).readBytes().toList()
            if (resultBytes != refBytes) {
                fail("comparing results failed Reference file is ${refFileName} (size: ${refBytes.size()})  and  result file is ${newFileName} (size: ${resultBytes.size()})  ")
            }
        }
    }

    /**
     * compares two inputStreams and return true if the inputStreams have the same content.
     *
     */
    public static boolean compare(InputStream in1, InputStream in2) throws IOException {
        int in1byte, in2byte;

        final int byteBufferSize = 10448;

        in1 = new BufferedInputStream(in1, byteBufferSize);
        in2 = new BufferedInputStream(in2, byteBufferSize);

        in1byte = 0;
        while (in1byte != -1) {
            // read one byte from file1
            in1byte = in1.read();

            // check if byte is whitespace or blank
            if ((!(Character.isWhitespace((char) in1byte))) && (in1byte != ' ') && (in1byte != '\n')
                    && (in1byte != '\r')) {
                // read one byte form file2
                in2byte = in2.read();

                // read bytes until byte is no whitespace or blank
                while ((Character.isWhitespace((char) in2byte)) || (in2byte == ' ') || (in2byte == '\n')
                        || (in2byte == '\r')) {
                    // if byte is whitespace or blank read next byte
                    in2byte = in2.read();
                }

                // check if byte from file1 and file2 are the same
                if (in1byte != in2byte) {
                    return false; // file content of the two files are not the same
                }
            }
        }
        return true;
    }

}
