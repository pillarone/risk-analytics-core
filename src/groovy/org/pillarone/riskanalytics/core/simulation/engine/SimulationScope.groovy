package org.pillarone.riskanalytics.core.simulation.engine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.model.DeterministicModel
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.CollectorFactory
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock

/**
 * The SimulationScope provides information, that is valid throughout the whole simulation.
 * For a smooth migration to the new simulation concept, the scope is used as a replacement for the SimulationContext.
 * Components using the SimulationContext are to be changed to use the corresponding scope.
 */

public class SimulationScope {

    private static Log LOG = LogFactory.getLog(SimulationScope)

    IterationScope iterationScope

    int numberOfIterations
    int iterationsDone

    Model model
    volatile Simulation simulation
    StructureInformation structureInformation
    Parameterization parameters
    ResultConfiguration resultConfiguration

    ParameterApplicator parameterApplicator
    ICollectorOutputStrategy outputStrategy

    MappingCache mappingCache

    List<SimulationBlock> simulationBlocks

    private volatile SimulationState simulationState = SimulationState.NOT_RUNNING

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation
        numberOfIterations = simulation.numberOfIterations
        parameters = simulation.parameterization
        resultConfiguration = simulation.template
    }

    public CollectorFactory getCollectorFactory() {
        return new CollectorFactory(outputStrategy)
    }

    int getProgress() {
        if (model instanceof DeterministicModel) {
            return iterationScope.periodScope.currentPeriod / iterationScope.numberOfPeriods * 100.0
        } else {
            return iterationsDone / numberOfIterations * 100.0
        }
    }

    @Deprecated
    public int getCurrentIteration() {
        LOG.info "currentIteration accessed via SimulationScope. You should use IterationScope"
        iterationScope.currentIteration
    }

    @Deprecated
    public int getCurrentPeriod() {
        LOG.info "currentPeriod accessed via SimulationScope. You should use PeriodScope"
        iterationScope.periodScope.currentPeriod
    }

    public DateTime getBeginOfFirstPeriodDate() {
        simulation.beginOfFirstPeriod
    }

    @Deprecated
    public DateTime getCurrentPeriodStartDate() {
        LOG.info "currentPeriodStartDate accessed via SimulationScope. You should use PeriodScope"
        iterationScope.periodScope.currentPeriodStartDate
    }

    @Deprecated
    public DateTime getNextPeriodStartDate() {
        LOG.info "nextPeriodStartDate accessed via SimulationScope. You should use PeriodScope"
        iterationScope.periodScope.nextPeriodStartDate
    }

    @Deprecated
    public boolean periodIncludesBeginningOfYear() {
        LOG.info "periodIncludesBeginningOfYear accessed via SimulationScope. You should use PeriodScope"
        iterationScope.periodScope.periodIncludesBeginningOfYear()
    }

    public SimulationState getSimulationState() {
        return this.simulationState
    }

    public void setSimulationState(SimulationState newState) {
        this.simulationState = newState
    }

    public void updateNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations
        simulation?.numberOfIterations = numberOfIterations
    }

}
