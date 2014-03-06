package org.pillarone.riskanalytics.core.simulation.engine
import models.core.CoreModel
import org.junit.Test
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

import static org.junit.Assert.*

class SimulationConfigurationTests {


    @Test
    void prepareForGrid() {
        FileImportService.importModelsIfNeeded(["Core"])

        Simulation simulation = new Simulation("prepareForGrid")
        simulation.modelClass = CoreModel
        simulation.parameterization = new Parameterization("CoreParameters", CoreModel)
        simulation.parameterization.load()

        simulation.template = new ResultConfiguration("CoreResultConfiguration", CoreModel)
        simulation.template.load()

        simulation.keyFiguresToPreCalculate = [:]

        SimulationConfiguration configuration = new SimulationConfiguration(simulation: simulation)
        configuration.prepareSimulationForGrid()

        assertNotSame(simulation, configuration.simulation)
        assertNotSame(simulation.parameterization, configuration.simulation.parameterization)
        assertNotSame(simulation.template, configuration.simulation.template)

        assertEquals(simulation.keyFiguresToPreCalculate, configuration.simulation.keyFiguresToPreCalculate)

        assertEquals(simulation.parameterization.
                allParameterHolders.size(), configuration.simulation.parameterization.allParameterHolders.size())
        for (ParameterHolder holder in simulation.parameterization.allParameterHolders) {
            ParameterHolder copiedHolder = configuration.simulation.parameterization.allParameterHolders.find {
                it.path == holder.path && it.periodIndex == holder.periodIndex
            }
            assertNotNull(copiedHolder)
            assertNotSame(copiedHolder, holder)
        }


    }
}
