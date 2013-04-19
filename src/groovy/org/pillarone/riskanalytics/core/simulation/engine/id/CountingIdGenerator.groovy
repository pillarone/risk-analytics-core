package org.pillarone.riskanalytics.core.simulation.engine.id

import groovy.transform.CompileStatic

import java.util.concurrent.atomic.AtomicInteger

@CompileStatic
class CountingIdGenerator implements IIdGenerator {

    private AtomicInteger integer

    CountingIdGenerator() {
        integer = new AtomicInteger(0)
    }

    String nextValue() {
        return integer.getAndIncrement().toString()
    }


}
