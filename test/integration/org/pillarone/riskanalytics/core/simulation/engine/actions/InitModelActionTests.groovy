package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.simulation.engine.MappingCache
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class InitModelActionTests extends GroovyTestCase {

    void testPerform() {

        SimulationScope simulationScope = new SimulationScope()
        simulationScope.mappingCache = new MappingCache()
        simulationScope.model = new EmptyModel()
        Action initAction = new InitModelAction(simulationScope: simulationScope)

        initAction.perform()
        // TODO (Sep 29, 2009, msh): find validation for init
        assertNotNull "model expected on simulationScope", simulationScope.model
        assertEquals "wrong model class", EmptyModel, simulationScope.model.class

    }

}