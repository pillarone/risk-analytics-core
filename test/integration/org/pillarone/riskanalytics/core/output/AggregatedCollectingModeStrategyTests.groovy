package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.packets.ITestPacketApple
import org.pillarone.riskanalytics.core.packets.ITestPacketOrange

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope

class AggregatedCollectingModeStrategyTests extends GroovyTestCase {

    AggregatedCollectingModeStrategy strategy
    SimulationRun run
    FieldMapping fieldMapping
    PathMapping pathMapping
    CollectorMapping collectorMapping

    void setUp() {
        fieldMapping = getFieldMapping("ultimate")
        pathMapping = getPathMapping("path")
        collectorMapping = getCollectorMapping("collector")

        run = new SimulationRun()

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope)
        periodScope.currentPeriod = 1
        iterationScope.currentIteration = 13
        simulationScope.simulationRun = run

        strategy = new AggregatedCollectingModeStrategy()
        PacketCollector collector = new PacketCollector(strategy)
        collector.simulationScope = simulationScope
        collector.path = "path"
        collector.collectorName = "collector"
    }

    void testCollectAndCreateResults() {
        PacketList<ITestPacketApple> claims = []
        def result = 0
        10.times {
            claims << new ITestPacketApple(ultimate: 1.1d)
            result += 1.1d
        }
        List<SingleValueResult> aggregatedValues = strategy.collect(claims)
        assertNotNull "no aggregatedValues", aggregatedValues
        assertFalse "empty values", aggregatedValues.isEmpty()
        assertEquals 1, aggregatedValues.size()
        assertEquals result, aggregatedValues.get(0).value

        SingleValueResult singleValueResult = aggregatedValues.get(0)

        assertSame "simulationRun", run, singleValueResult.simulationRun
        assertEquals "period", 1, singleValueResult.period
        assertEquals "iteration", 13, singleValueResult.iteration
        assertSame "pathMapping", pathMapping, singleValueResult.path
        assertSame "collector", collectorMapping, singleValueResult.collector
        assertSame "field", fieldMapping, singleValueResult.field
        assertEquals "valueIndex", 0, singleValueResult.valueIndex

        PacketList<ITestPacketOrange> underwritingInfos = []
        def aResult = 0
        def bResult = 0
        10.times {
            underwritingInfos << new ITestPacketOrange(a: 1.5d, b: 2.2d)
            aResult += 1.5d
            bResult += 2.2d
        }
        aggregatedValues = strategy.collect(underwritingInfos)
        assertNotNull "no aggregatedValues", aggregatedValues
        assertFalse "empty values", aggregatedValues.isEmpty()
        assertEquals aResult, aggregatedValues.find {it.field.fieldName == "a"}.value
        assertEquals bResult, aggregatedValues.find {it.field.fieldName == "b"}.value

    }

    private FieldMapping getFieldMapping(String fieldName) {
        FieldMapping fieldMapping = FieldMapping.findByFieldName(fieldName)
        if (!fieldMapping) {
            fieldMapping = new FieldMapping(fieldName: fieldName)
            assertNotNull fieldMapping.save()
        }
        return fieldMapping
    }

    private CollectorMapping getCollectorMapping(String collectorName) {
        CollectorMapping collectorMapping = CollectorMapping.findByCollectorName(collectorName)
        if (!collectorMapping) {
            collectorMapping = new CollectorMapping(collectorName: collectorName)
            assertNotNull collectorMapping.save()
        }
        return collectorMapping
    }

    private PathMapping getPathMapping(String pathName) {
        PathMapping pathMapping = PathMapping.findByPathName(pathName)
        if (!pathMapping) {
            pathMapping = new PathMapping(pathName: pathName)
            assertNotNull pathMapping.save()
        }
        return pathMapping
    }
}