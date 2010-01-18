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

class RunSimulationServiceTests extends GrailsUnitTestCase {

    def runSimulationService

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

        def parameter = ParameterizationDAO.findByName('CoreParameters')
        assertNotNull parameter

        def resultConfig = ResultConfigurationDAO.findByName('CoreResultConfiguration')
        assertNotNull resultConfig


        SimulationRun run = new SimulationRun()
        run.name = "Core_${new Date()}"
        run.parameterization = parameter
        run.resultConfiguration = resultConfig
        run.model = CoreModel.name
        run.modelVersionNumber = "1"
        run.periodCount = 1
        run.iterations = 1000

        if (!run.save()) {

            run.errors.each {
                log.error it
                fail("Error saving SimulationRun")
            }
        }

        SimulationConfiguration simulationConfiguration = new SimulationConfiguration()
        simulationConfiguration.simulationRun = run
        simulationConfiguration.outputStrategy = new NoOutput()

        SimulationRunner runner = runSimulationService.runSimulation(SimulationRunner.createRunner(), simulationConfiguration)
        assertNotNull runner

        while (runner.currentScope.iterationScope.currentIteration < 999) {
            log.info "current iteration ${runner.currentScope.iterationScope.currentIteration}"
            Thread.sleep(2000)
            assertNull "simulation crahed", runner.error
        }
    }
}