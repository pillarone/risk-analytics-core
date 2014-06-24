package org.pillarone.riskanalytics.core.simulation.engine.id

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

import java.util.concurrent.atomic.AtomicInteger

@CompileStatic
class IdGenerator implements IIdGenerator {

    private SimulationScope simulationScope
    private AtomicInteger integer

    IdGenerator(SimulationScope simulationScope) {
        this.simulationScope = simulationScope
        integer = new AtomicInteger(0)
    }

    synchronized String nextValue() {
        return simulationScope.iterationScope.currentIteration + "|" + integer.getAndIncrement().toString()
    }


}
