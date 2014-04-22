package org.pillarone.riskanalytics.core.modellingitem

import models.core.CoreModel
import org.junit.Test
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.output.SimulationRun

class CacheItemMapperTests {

    @Test
    void testMapSimulations_ToBeDeleted() {
        SimulationRun run = SimulationRun.withNewSession {
            SimulationRun run = new SimulationRun()
            run.name = 'name'
            run.model = CoreModel.class.name
            run.save(flush: true)
            run
        }
        run.toBeDeleted = true
        SimulationCacheItem item = CacheItemMapper.getModellingItem(run, true)
        assert item.parameterization == null
        assert item.resultConfiguration == null
    }

    @Test(expected = IllegalStateException)
    void testMapSimulations_ToBeDeleted_butNotForDeletion() {
        SimulationRun run = SimulationRun.withNewSession {
            SimulationRun run = new SimulationRun()
            run.name = 'illegal'
            run.model = CoreModel.class.name
            run.save(flush: true)
            run
        }
        run.toBeDeleted = true
        CacheItemMapper.getModellingItem(run)
    }

    // PMO-2681
    @Test
    void testMappingModelForSimulations() {
        def versionString = "9.9.9.9"
        SimulationRun run = SimulationRun.withNewSession {
            def model = new ModelDAO(name: 'name', itemVersion: versionString, modelClassName: 'no matter', srcCode: 'goto fail goto fail').save(flush: true)
            def run = new SimulationRun(
                    model: CoreModel.name,
                    usedModel: model,
                    name: 'supi'
            )
            run.save(flush: true)
        }
        SimulationCacheItem item = CacheItemMapper.getModellingItem(run)
        assert item.modelVersionNumber.toString() == versionString
        assert item.modelClass == CoreModel
    }
}
