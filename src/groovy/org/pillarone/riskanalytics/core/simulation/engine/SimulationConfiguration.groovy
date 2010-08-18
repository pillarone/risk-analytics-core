package org.pillarone.riskanalytics.core.simulation.engine

import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock

/**
 * The SimulationConfiguration is a descriptor for a runnable simulation. All runtime aspects e.g. numberOfIterations,
 * numberOfPeriods, the parameterization, etc are stored in the simulationRun. They have to be persistent.
 * The way how results get stored is given with the outputStrategy
 *
 * Use the SimulationConfiguration to configure a SimulationRunner instance.
 */
public class SimulationConfiguration implements Serializable, Cloneable {

    Simulation simulation
    ICollectorOutputStrategy outputStrategy
    MappingCache mappingCache
    List<SimulationBlock> simulationBlocks = []

    private static Log LOG = LogFactory.getLog(SimulationConfiguration)

    /**
     * This creates a new Simulation instance based on the existing one, which only contains the necessary info for the
     * simulation to make sure that this object can be serialized to the grid.
     */
    void prepareSimulationForGrid() {
        Simulation preparedSimulation = new Simulation(simulation.name)
        preparedSimulation.id = simulation.id
        preparedSimulation.numberOfIterations = simulation.numberOfIterations
        preparedSimulation.beginOfFirstPeriod = simulation.beginOfFirstPeriod
        preparedSimulation.randomSeed = simulation.randomSeed
        preparedSimulation.modelClass = simulation.modelClass
        preparedSimulation.periodCount = simulation.periodCount

        preparedSimulation.parameterization = new Parameterization(simulation.parameterization.name)
        preparedSimulation.parameterization.periodCount = simulation.parameterization.periodCount
        //clone parameters to make sure they don't have any model or component references
        preparedSimulation.parameterization.parameterHolders = simulation.parameterization.parameterHolders.collect { it.clone() }


        preparedSimulation.template = new ResultConfiguration(simulation.template.name)
        preparedSimulation.template.collectors = simulation.template.collectors

        preparedSimulation.structure = ModelStructure.getStructureForModel(simulation.modelClass)

        this.simulation = preparedSimulation
    }

    SimulationConfiguration clone() {
        SimulationConfiguration configuration = (SimulationConfiguration) super.clone()
        configuration.simulationBlocks = []
        return configuration
    }

    void addSimulationBlock(SimulationBlock simulationBlock) {
        simulationBlocks << simulationBlock
        calculateTotalIterations()
    }

    private void calculateTotalIterations() {
        simulation.numberOfIterations = simulationBlocks*.blockSize.sum()
    }

}