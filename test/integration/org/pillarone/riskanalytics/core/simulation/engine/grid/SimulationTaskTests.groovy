package org.pillarone.riskanalytics.core.simulation.engine.grid


import org.gridgain.grid.Grid
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.gridgain.grid.GridNode


class SimulationTaskTests extends GroovyTestCase {

    void testSplitOneJobOneBlock() {
        SimulationTask simulationTask = new TestSimulationTask(1)

        SimulationConfiguration configuration = createConfig(999)

        //tests will have to be adjusted for other values
        assertEquals 1000, SimulationTask.SIMULATION_BLOCK_SIZE
        List<GridNode> mockNodes = new ArrayList<GridNode>();
        mockNodes.add(null);

        Collection jobs = simulationTask.map(mockNodes, configuration).keySet();
        assertEquals 1, jobs.size()

        SimulationConfiguration runner = jobs.iterator().next().simulationConfiguration
        assertEquals 1, runner.simulationBlocks.size()

        SimulationBlock block = runner.simulationBlocks[0]
        assertEquals 0, block.streamOffset
        assertEquals 0, block.iterationOffset
        assertEquals 999, block.blockSize

        assertNotNull configuration.simulation.start
        assertNull configuration.simulation.end
    }

    void testSplitOneJobMultipleBlocks() {
        SimulationTask simulationTask = new TestSimulationTask(1)

        SimulationConfiguration configuration = createConfig(2500)

        //tests will have to be adjusted for other values
        assertEquals 1000, SimulationTask.SIMULATION_BLOCK_SIZE

        List<GridNode> mockNodes = new ArrayList<GridNode>();
        mockNodes.add(null);

        Collection jobs = simulationTask.map(mockNodes, configuration).keySet();

        assertEquals 1, jobs.size()

        SimulationConfiguration runner = jobs.iterator().next().simulationConfiguration
        assertEquals 3, runner.simulationBlocks.size()

        List<SimulationBlock> blocks = runner.simulationBlocks.sort { it.streamOffset }
        SimulationBlock block = blocks[0]
        assertEquals 0, block.streamOffset
        assertEquals 0, block.iterationOffset
        assertEquals 1000, block.blockSize

        block = blocks[1]
        assertEquals 1, block.streamOffset
        assertEquals 1000, block.iterationOffset
        assertEquals 1000, block.blockSize

        block = blocks[2]
        assertEquals 2, block.streamOffset
        assertEquals 2000, block.iterationOffset
        assertEquals 500, block.blockSize

        assertNotNull configuration.simulation.start
        assertNull configuration.simulation.end
    }

    void testSplitTwoJobsMultipleBlocks() {
        SimulationTask simulationTask = new TestSimulationTask(2)

        SimulationConfiguration configuration = createConfig(2500)

        //tests will have to be adjusted for other values
        assertEquals 1000, SimulationTask.SIMULATION_BLOCK_SIZE

        List<GridNode> mockNodes = new ArrayList<GridNode>();
        mockNodes.add(null);

        Collection jobs = simulationTask.map(mockNodes, configuration).keySet();
        assertEquals 2, jobs.size()
        jobs = jobs.sort { it.simulationConfiguration.simulationBlocks.size() }

        SimulationConfiguration runner = jobs.iterator().next().simulationConfiguration
        assertEquals 1, runner.simulationBlocks.size()

        assertEquals 1, runner.simulationBlocks[0].streamOffset
        assertEquals 1000, runner.simulationBlocks[0].iterationOffset
        assertEquals 1000, runner.simulationBlocks[0].blockSize

        Iterator it = jobs.iterator()
        while (it.hasNext())
            runner = it.next().simulationConfiguration;

        assertEquals 2, runner.simulationBlocks.size()
        List<SimulationBlock> blocks = runner.simulationBlocks.sort { it.streamOffset }
        SimulationBlock block = blocks[0]
        assertEquals 0, block.streamOffset
        assertEquals 0, block.iterationOffset
        assertEquals 1000, block.blockSize

        block = blocks[1]
        assertEquals 2, block.streamOffset
        assertEquals 2000, block.iterationOffset
        assertEquals 500, block.blockSize

        assertNotNull configuration.simulation.start
        assertNull configuration.simulation.end
    }

    SimulationConfiguration createConfig(int iterationCount) {
        SimulationConfiguration configuration = new SimulationConfiguration()
        Simulation simulation = new Simulation("test")
        simulation.id = 1L
        simulation.numberOfIterations = iterationCount
        configuration.simulation = simulation
        assertNull simulation.start
        assertNull simulation.end

        return configuration
    }
}


class TestSimulationTask extends SimulationTask {

    int cpuCount

    def TestSimulationTask(cpuCount) {
        this.cpuCount = cpuCount;
    }

    protected int getTotalProcessorCount(Grid grid) {
        return cpuCount
    }


}

