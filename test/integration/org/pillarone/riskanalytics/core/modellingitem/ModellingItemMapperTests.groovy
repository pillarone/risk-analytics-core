package org.pillarone.riskanalytics.core.modellingitem

import models.core.CoreModel
import org.junit.Test
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.Simulation

/**
 * Created with IntelliJ IDEA.
 * User: detlef
 * Date: 22.08.13
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
class ModellingItemMapperTests {

    @Test
    void testMapSimulations_ToBeDeleted() {
        SimulationRun run = new SimulationRun()
        run.name = 'name'
        run.model = CoreModel.class.name
        run.toBeDeleted = true
        run.save()
        Simulation item = ModellingItemMapper.getModellingItem(run)
        assert item.parameterization == null
        assert item.template == null
    }

    // PMO-2681
    @Test
    void testMappingModelForSimulations() {
        def versionString = "9.9.9.9"
        SimulationRun run = new SimulationRun(model: CoreModel.name, usedModel: new ModelDAO(itemVersion: versionString))
        Simulation item = ModellingItemMapper.getModellingItem(run)
        assert item.modelVersionNumber.toString() == versionString
        assert item.modelClass == CoreModel
    }
}
