package org.pillarone.riskanalytics.core.simulation.engine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.output.CollectorFactory
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.model.ModelHelper
import org.pillarone.riskanalytics.core.output.ICollectingModeStrategy
import org.pillarone.riskanalytics.core.output.CollectingModeFactory
import org.pillarone.riskanalytics.core.wiring.IPacketListener

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
    IPacketListener packetListener;

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
        preparedSimulation.parameterization.versionNumber = simulation.parameterization.versionNumber

        //clone parameters to make sure they don't have any model or component references
        preparedSimulation.parameterization.parameterHolders = simulation.parameterization.parameterHolders.collect { it.clone() }


        preparedSimulation.template = new ResultConfiguration(simulation.template.name)
        preparedSimulation.template.versionNumber = simulation.template.versionNumber
        preparedSimulation.template.collectors = simulation.template.collectors

        preparedSimulation.structure = ModelStructure.getStructureForModel(simulation.modelClass)
        preparedSimulation.modelVersionNumber = simulation.modelVersionNumber

        this.simulation = preparedSimulation
    }

    SimulationConfiguration clone() {
        SimulationConfiguration configuration = (SimulationConfiguration) super.clone()
        configuration.simulationBlocks = []
        return configuration
    }

    void addSimulationBlock(SimulationBlock simulationBlock) {
        simulationBlocks << simulationBlock
    }

    private void calculateTotalIterations() {
        simulation.numberOfIterations = simulationBlocks*.blockSize.sum()
    }

     /**
     * Determines all possible path & field values for this simulation and persists them if they do not exist yet, because we do not have any DB access
     * during a grid job.
     * @param simulationConfiguration the simulation details
     * @return a mapping cache filled with all necessary mappings for this simulation.
     */
    MappingCache createMappingCache(ResultConfiguration resultConfiguration) {
        Model model = simulation.modelClass.newInstance()
        model.init()

        ParameterApplicator parameterApplicator = new ParameterApplicator(model: model, parameterization: simulation.parameterization)
        parameterApplicator.init()
        parameterApplicator.applyParameterForPeriod(0)

        SimulationRunner runner = SimulationRunner.createRunner()
        CollectorFactory collectorFactory = runner.currentScope.collectorFactory
        List<PacketCollector> drillDownCollectors = resultConfiguration.getResolvedCollectors(model, collectorFactory)
        List<String> drillDownPaths = getDrillDownPaths(drillDownCollectors)
        Set paths = ModelHelper.getAllPossibleOutputPaths(model, drillDownPaths)

        Set fields = ModelHelper.getAllPossibleFields(model)
        MappingCache cache = new MappingCache()
        cache.initCache(model)

        for (String path in paths) {
            cache.lookupPathDB(path)
        }

        for (String field in fields) {
            cache.lookupField(field)
        }

        this.mappingCache = cache
    }

    private List<String> getDrillDownPaths(List<PacketCollector> collectors) {
        List<String> paths = []
        // todo: requires a proper refactoring as the core plugin itself knows nothing about the aggregate drill down collector
        ICollectingModeStrategy drillDownCollector = CollectingModeFactory.getStrategy("AGGREGATED_DRILL_DOWN")
        addMatchingCollector(drillDownCollector, collectors, paths)
        ICollectingModeStrategy splitPerSourceCollector = CollectingModeFactory.getStrategy("SPLIT_PER_SOURCE")
        addMatchingCollector(splitPerSourceCollector, collectors, paths)
        return paths
    }

    private addMatchingCollector(ICollectingModeStrategy collectorModeStrategy, List<PacketCollector> collectors, ArrayList<String> paths) {
        if (collectorModeStrategy != null) {
            for (PacketCollector collector: collectors) {
                if (collector.mode.class.equals(collectorModeStrategy.class)) {
                    paths << collector.path
                }
            }
        }
    }

}