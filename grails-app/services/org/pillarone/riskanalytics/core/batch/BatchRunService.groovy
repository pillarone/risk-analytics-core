package org.pillarone.riskanalytics.core.batch

import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationQueueService
import org.pillarone.riskanalytics.core.simulation.item.*
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

import java.text.SimpleDateFormat

class BatchRunService {

    SimulationQueueService simulationQueueService

    private static
    final String BATCH_SIMNAME_STAMP_FORMAT = System.getProperty("BatchRunService.BATCH_SIMNAME_STAMP_FORMAT", "yyyyMMdd HH:mm:ss z")

    void runBatch(Batch batch) {
        batch.load()
        if (!batch.executed) {
            offer(createSimulations(batch))
            batch.executed = true
            batch.save()
        }
    }

    private List<Simulation> createSimulations(Batch batch) {
        Map<Class, SimulationProfile> byModelClass = getSimulationProfilesGroupedByModelClass(batch.simulationProfileName)
        batch.parameterizations.collect {
            createSimulation(it, byModelClass[it.modelClass], batch)
        }
    }

    void runBatchRunSimulation(Simulation simulationRun) {
        offer(simulationRun)
    }

    private static boolean shouldRun(Simulation run) {
        run.end == null && run.start == null
    }

    private void offer(List<Simulation> simulationRuns) {
        List<SimulationConfiguration> configurations = simulationRuns.findAll { Simulation simulationRun -> shouldRun(simulationRun) }.collect {
            new SimulationConfiguration(it)
        }
        configurations.each { start(it) }
    }

    private void offer(Simulation simulation) {
        if (shouldRun(simulation)) {
            start(new SimulationConfiguration(simulation))
        }
    }

    private void start(SimulationConfiguration simulationConfiguration) {
        simulationQueueService.offer(simulationConfiguration, 5)
    }

    boolean deleteBatch(Batch batch) {
        SimulationRun.withTransaction {
            SimulationRun.withBatchRunId(batch.id).list().each {
                it.batchRun = null
                it.save()
            }
            batch.delete()
        }
    }

    Batch createBatch(List<Parameterization> parameterizations) {
        def batch = new Batch(new SimpleDateFormat(BATCH_SIMNAME_STAMP_FORMAT).format(new Date()))
        batch.parameterizations = parameterizations
        batch.executed = false
        batch.save()
        batch
    }

    private
    static Simulation createSimulation(Parameterization parameterization, SimulationProfile simulationProfile, Batch batch = null) {
        parameterization.load()
        String name = "batch " + parameterization.name + " " + new SimpleDateFormat(BATCH_SIMNAME_STAMP_FORMAT).format(new Date())
        Simulation simulation = new Simulation(name)
        simulation.modelClass = parameterization.modelClass
        simulation.parameterization = parameterization
        simulation.structure = ModelStructure.getStructureForModel(parameterization.modelClass)
        simulation.batch = batch
        simulation.template = simulationProfile.template
        //TODO decide if we need it and should add it to simulation profiles
        //simulation.beginOfFirstPeriod = beginOfFirstPeriod

        simulation.numberOfIterations = simulationProfile.numberOfIterations
        simulation.periodCount = parameterization.periodCount
        if (simulation.randomSeed != null) {
            simulation.randomSeed = simulationProfile.randomSeed
        } else {
            long millis = System.currentTimeMillis()
            long millisE5 = millis / 1E5
            simulation.randomSeed = millis - millisE5 * 1E5
        }

        for (ParameterHolder holder in simulationProfile.runtimeParameters) {
            simulation.addParameter(holder)
        }
        simulation.save()
        return simulation
    }

    Map<Class, SimulationProfile> getSimulationProfilesGroupedByModelClass(String simulationProfileName) {
        Map<Class, SimulationProfile> result = [:]
        SimulationProfileDAO.findAllByName(simulationProfileName).collect {
            SimulationProfile simulationProfile = new SimulationProfile(it.name, getClass().classLoader.loadClass(it.modelClassName))
            simulationProfile.load()
            simulationProfile
        }.each {
            result[it.modelClass] = it
        }
        result
    }

    List<String> getSimulationProfileNames() {
        SimulationProfileDAO.createCriteria().list {
            projections {
                property('name')
            }
        }.unique()
    }

    Simulation findSimulation(Batch batch, Parameterization parameterization) {
        SimulationRun run = SimulationRun.withBatchRunId(batch?.id).withParamId(parameterization?.id).get()
        if (run) {
            Simulation simulation = new Simulation(run.name)
            simulation.load()
            return simulation
        }
        return null
    }
}
