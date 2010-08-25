package org.pillarone.riskanalytics.core.simulation.engine

import grails.test.GrailsUnitTestCase
import models.core.CoreModel
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.NoOutput
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.pillarone.riskanalytics.core.simulation.SimulationState

class RunSimulationServiceTests extends GrailsUnitTestCase {

    RunSimulationService runSimulationService

    private static Log log = LogFactory.getLog(SimulationRunner)

    void testGetService() {

        def service = RunSimulationService.getService()
        assertNotNull service
        assertTrue RunSimulationService.class.isAssignableFrom(service.class)

    }

    void testRunSimulation() {

        new ParameterizationImportService().compareFilesAndWriteToDB(["CoreParameters"])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(["CoreResultConfiguration"])
        new ModelStructureImportService().compareFilesAndWriteToDB(["CoreStructure"])

        def parameter = new Parameterization('CoreParameters')
        parameter.load()

        def resultConfig = new ResultConfiguration('CoreResultConfiguration')
        resultConfig.load()


        Simulation run = new Simulation("Core_${new Date().toString()}")
        run.parameterization = parameter
        run.template = resultConfig
        run.modelClass = CoreModel
        run.modelVersionNumber = new VersionNumber("1")
        run.periodCount = 1
        run.numberOfIterations = 1000
        run.structure = ModelStructure.getStructureForModel(CoreModel)
        run.save()

        SimulationConfiguration simulationConfiguration = new SimulationConfiguration()
        simulationConfiguration.simulation = run
        simulationConfiguration.outputStrategy = new NoOutput()

        SimulationTask runner = runSimulationService.runSimulationOnGrid(simulationConfiguration)
        assertNotNull runner

        while (runner.simulationState != SimulationState.FINISHED) {
            Thread.sleep(2000)
            assertTrue "simulation crahed", runner.simulationErrors.empty
        }
    }
}