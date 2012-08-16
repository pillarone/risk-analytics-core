package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.ITestPacketApple
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.simulation.engine.MappingCache
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.simulation.item.Simulation


class SingleValueCollectingModeStrategyTests extends GroovyTestCase {

    void testAddAggregatedValue() {

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope)
        simulationScope.mappingCache = MappingCache.instance
        simulationScope.simulation = new Simulation("name")

        SingleValueCollectingModeStrategy mode = new SingleValueCollectingModeStrategy()
        PacketCollector collector = new PacketCollector(mode)
        collector.simulationScope = simulationScope
        collector.path = "somePath"

        PacketList<ITestPacketApple> list = new PacketList<ITestPacketApple>()
        list.add(new ITestPacketApple(value: 5))
        list.add(new ITestPacketApple(value: 10))
        final List<SingleValueResultPOJO> results = mode.collect(list, true)
        assertEquals(3, results.size())

        assertEquals(5, results[0].value)
        assertEquals(SingleValueCollectingModeStrategy.IDENTIFIER, results[0].collector.collectorName)

        assertEquals(10, results[1].value)
        assertEquals(SingleValueCollectingModeStrategy.IDENTIFIER, results[1].collector.collectorName)

        assertEquals(15, results[2].value)
        assertEquals(AggregatedWithSingleAvailableCollectingModeStrategy.IDENTIFIER, results[2].collector.collectorName)
    }
}
