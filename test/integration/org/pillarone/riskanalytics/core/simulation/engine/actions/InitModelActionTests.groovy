package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.junit.Test
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.simulation.engine.MappingCache
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class InitModelActionTests {

    @Test
    void testPerform() {

        Simulation simulation = new Simulation("simulation")
        simulation.parameterization = new Parameterization("p14n", EmptyModel)
        simulation.parameterization.modelVersionNumber = new VersionNumber("1")

        SimulationScope simulationScope = new SimulationScope(simulation: simulation)
        simulationScope.mappingCache = new MappingCache()
        simulationScope.model = new EmptyModel()
        Action initAction = new InitModelAction(simulationScope: simulationScope)

        initAction.perform()
        // TODO (Sep 29, 2009, msh): find validation for init
        assertNotNull "model expected on simulationScope", simulationScope.model
        assertEquals "wrong model class", EmptyModel, simulationScope.model.class

    }

    @Test
    void testCheckVersion() {

        Simulation simulation = new Simulation("simulation")
        simulation.parameterization = new Parameterization("p14n", EmptyModel)
        simulation.parameterization.modelVersionNumber = new VersionNumber("2")

        SimulationScope simulationScope = new SimulationScope(simulation: simulation)
        simulationScope.mappingCache = new MappingCache()
        simulationScope.model = new EmptyModel()
        Action initAction = new InitModelAction(simulationScope: simulationScope)

        shouldFail(IllegalStateException, {
            initAction.perform()
        })

    }

}