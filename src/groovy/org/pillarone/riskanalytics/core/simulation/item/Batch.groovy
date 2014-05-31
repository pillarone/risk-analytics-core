package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.output.SimulationRun

class Batch extends ModellingItem {

    List<Parameterization> parameterizations = []
    String comment
    String simulationProfileName

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
        batchRun.creationDate = creationDate
        batchRun.modificationDate = modificationDate
        batchRun.lastUpdater = lastUpdater
        batchRun.creator = creator
        batchRun.simulationProfileName = simulationProfileName
        batchRun.parameterizations = parameterizations.collect {
            it.loadFromDB()
        }
    }

    @Override
    protected void mapFromDao(def Object dao, boolean completeLoad) {
        BatchRun batchRun = dao as BatchRun
        comment = batchRun.comment
        executed = batchRun.executed
        name = batchRun.name
        simulationProfileName = batchRun.simulationProfileName
        creationDate = batchRun.creationDate
        modificationDate = batchRun.modificationDate
        lastUpdater = batchRun.lastUpdater
        creator = batchRun.creator
        parameterizations = batchRun.parameterizations.collect { ParameterizationDAO run ->
            Parameterization parameterization = new Parameterization(run.name)
            parameterization.versionNumber = run.itemVersion ? new VersionNumber(run.itemVersion) : null
            parameterization.modelClass = getClass().classLoader.loadClass(run.modelClassName)
            parameterization.load(false)  //PMO-2802 Don't fully load p14ns - speed up opening a batch dramatically
            parameterization
        }
    }

    boolean isValidToRun() {
        if (executed) {
            return false
        }
        List<String> modelNames = parameterizations.modelClass.name.unique()
        return modelNames.every {
            SimulationProfileDAO.countByNameAndModelClassName(simulationProfileName, it) > 0
        }
    }

    @Override
    protected Object deleteDaoImpl(Object dao) {
        BatchRun batchRun = dao as BatchRun
        SimulationRun.withBatchRunId(batchRun.id).list().each {
            it.batchRun = null
            it.save()
        }
        super.deleteDaoImpl(batchRun)
    }

    @Override
    protected loadFromDB() {
        return BatchRun.findByName(name)
    }

    @Override
    Class getModelClass() {
        null
    }

    @Override
    List<Simulation> getSimulations() {
        SimulationRun.withBatchRunId(id).list().collect {
            Simulation simulation = new Simulation(it.name)
            simulation.load(false) //PMO-2802 Don't fully load sims too
            simulation
        }
    }
}
