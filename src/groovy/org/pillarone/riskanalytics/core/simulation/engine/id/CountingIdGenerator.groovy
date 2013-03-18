package org.pillarone.riskanalytics.core.simulation.engine.id

import java.util.concurrent.atomic.AtomicInteger


class CountingIdGenerator implements IIdGenerator {

    private AtomicInteger integer

    CountingIdGenerator() {
        integer = new AtomicInteger(0)
    }

    String nextValue() {
        return integer.getAndIncrement().toString()
    }


}
