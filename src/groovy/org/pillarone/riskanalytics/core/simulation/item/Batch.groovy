package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.output.SimulationRun

class Batch extends ModellingItem {

    List<Simulation> simulations = []
    String comment
    boolean executed = false

    Batch(String name) {
        super(name)
    }

    @Override
    protected createDao() {
        new BatchRun(name: name)
    }

    @Override
    def getDaoClass() {
        BatchRun
    }

    @Override
    protected void mapToDao(def dao) {
        BatchRun batchRun = dao as BatchRun
        batchRun.comment = comment
        batchRun.name = name
        batchRun.executed = executed
        batchRun.simulationRuns = simulations.collect {
            SimulationRun run = SimulationRun.findByName(it.name)
            //TODO run == null ?
            run
        }
    }

    @Override
    protected void mapFromDao(def Object dao, boolean completeLoad) {
        BatchRun batchRun = dao as BatchRun
        comment = batchRun.comment
        executed = batchRun.executed
        name = batchRun.name
        simulations = batchRun.simulationRuns.collect { SimulationRun run ->
            Simulation simulation = new Simulation(run.name)
            simulation.load()
            simulation
        }
    }

    @Override
    protected loadFromDB() {
        return BatchRun.findByName(name)
    }

    @Override
    Class getModelClass() {
        null
    }
}
