package org.pillarone.riskanalytics.core.simulation.engine

import models.core.CoreModel
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.FileOutput
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock
import org.pillarone.riskanalytics.core.simulation.item.*
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.wiring.WireCategory

import java.util.concurrent.CyclicBarrier

/**
 * An abstract class which provides functionality to run model tests.
 * This class does not belong to the test sources so that it can be used in plugins too.
 */
abstract class DependingModelTest extends ModelTest {

    protected SimulationRunner runner2
    Simulation run2

    @Override
    protected void setUp() {
        super.setUp()
        def parameter = ParameterizationDAO.findByName(getParameterDisplayName())
        assertNotNull parameter

        def resultConfig = ResultConfigurationDAO.findByName(getResultConfigurationDisplayName())
        assertNotNull resultConfig

        Class modelClass = getModelClass()

        run2 = prepareSimulation(parameter.name, resultConfig.name, modelClass)
        assertNotNull run2.save()
    }

    final void testModelRun() {
        runner = prepareRunner(run)
        runner2 = prepareRunner(run2)

        new SimulationRunnerSynchronizer([runner, runner2], createExtraWiringClosure([runner, runner2]))

        def t1 = Thread.start { runner.start(); assertNull "${runner.error?.error?.message}", runner.error }
        def t2 = Thread.start { runner2.start(); assertNull "${runner2.error?.error?.message}", runner2.error }

        t1.join()
        t2.join()

        if (shouldCompareResults()) {
            compareResults()
        }
        postSimulationEvaluation()
    }

    abstract Closure createExtraWiringClosure(List<SimulationRunner> runners)


}