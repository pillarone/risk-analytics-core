package org.pillarone.riskanalytics.core.simulation.engine

import org.gridgain.grid.Grid
import org.gridgain.grid.GridTaskFuture
import org.gridgain.grid.typedef.CI1
import org.pillarone.riskanalytics.core.BatchRunSimulationRun
import org.pillarone.riskanalytics.core.cli.ImportStructureInTransaction
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.batch.OutputStrategyFactory
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationHandler
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.pillarone.riskanalytics.core.simulation.engine.grid.SpringBeanDefinitionRegistry
import org.pillarone.riskanalytics.core.simulation.item.Simulation

import javax.annotation.PostConstruct

class SimulationQueueService {
    private final List<ISimulationQueueListener> listeners = []
    private final PriorityQueue<QueueEntry> queue = new PriorityQueue<QueueEntry>()
    private final Object lock = new Object()
    private CurrentTask currentTask
    private CI1<GridTaskFuture> taskListener
    private boolean busy = false
    Grid grid

    @PostConstruct
    void initialize() {
        taskListener = new CI1<GridTaskFuture>() {
            @Override
            void apply(GridTaskFuture future) {
                synchronized (lock) {
                    if (!currentTask) {
                        throw new IllegalStateException('simulation ended, but there is no currentTask')
                    }
                    busy = false
                    QueueEntry entry = currentTask.entry
                    currentTask = null
                    future.stopListenAsync(taskListener)
                    notifyFinished(entry)
                    poll()
                }
            }
        }
        addSimulationQueueListener(new AddOrRemoveLockedTagListener())
        BatchRunSimulationRun.listOrderByPriority().each { offer(it) }
    }

    private void notifyStarting(QueueEntry queueEntry) {
        synchronized (listeners) {
            listeners.each { it.starting(queueEntry) }
        }
    }

    private void notifyFinished(QueueEntry queueEntry) {
        synchronized (listeners) {
            listeners.each { it.finished(queueEntry) }
        }
    }

    private void notifyOffered(QueueEntry queueEntry) {
        synchronized (listeners) {
            listeners.each { it.offered(queueEntry) }
        }
    }

    private void notifyRemoved(UUID uuid) {
        synchronized (listeners) {
            listeners.each { it.removed(uuid) }
        }
    }

    void addSimulationQueueListener(ISimulationQueueListener listener) {
        synchronized (listeners) {
            listeners.add(listener)
        }
    }

    void removeSimulationQueueListener(ISimulationQueueListener listener) {
        synchronized (listeners) {
            listeners.remove(listener)
        }
    }

    private void poll() {
        synchronized (lock) {
            if (busy) {
                return
            }
            QueueEntry queueEntry = queue.poll()
            if (queueEntry) {
                busy = true
                Thread.start {
                    start(queueEntry)
                }
                notifyStarting(queueEntry)
            }
        }
    }

    private void start(QueueEntry queueEntry) {
        SimulationConfiguration configuration = queueEntry.simulationConfiguration
        SimulationRun.withTransaction {
            configuration.createMappingCache(configuration.simulation.template)
        }
        configuration.prepareSimulationForGrid()
        configuration.beans = SpringBeanDefinitionRegistry.requiredBeanDefinitions
        GridTaskFuture gridTaskFuture = grid.execute(queueEntry.simulationTask, queueEntry.simulationConfiguration)
        gridTaskFuture.listenAsync(taskListener)
        synchronized (lock) {
            currentTask = new CurrentTask(gridTaskFuture: gridTaskFuture, entry: queueEntry)
        }
    }


    QueueEntry[] getSortedQueueEntries() {
        synchronized (lock) {
            queue.toArray() as QueueEntry[]
        }
    }


    static class CurrentTask {
        GridTaskFuture gridTaskFuture
        QueueEntry entry
    }

    SimulationHandler offer(SimulationConfiguration configuration, int priority = 10) {
        preConditionCheck(configuration)
        QueueEntry queueEntry = new QueueEntry(new SimulationTask(), configuration, priority)
        synchronized (lock) {
            queue.offer(queueEntry)
        }
        notifyOffered(queueEntry)
        poll()
        new SimulationHandler(queueEntry.simulationTask, queueEntry.id)
    }


    void offer(BatchRunSimulationRun batchRunSimulationRun) {
        SimulationRun run = batchRunSimulationRun.simulationRun
        if (run.endTime == null && run.startTime == null) {
            ICollectorOutputStrategy strategy = OutputStrategyFactory.getInstance(batchRunSimulationRun.strategy)
            Simulation simulation = loadSimulation(batchRunSimulationRun.simulationRun.name)
            SimulationConfiguration configuration = new SimulationConfiguration(simulation: simulation, outputStrategy: strategy)
            ImportStructureInTransaction.importStructure(configuration)
            offer(configuration, 5)
            return
        }
        if (run.endTime != null) {
            log.info "simulation ${run.name} already executed at ${run.endTime}"
            return
        }
        log.info "simulation ${batchRunSimulationRun.simulationRun.name} is already running"
    }

    private Simulation loadSimulation(String simulationName) {
        Simulation simulation = new Simulation(simulationName)
        simulation.load()
        simulation.parameterization.load();
        simulation.template.load();
        return simulation
    }

    private static void preConditionCheck(SimulationConfiguration configuration) {
        Long id = configuration?.simulation?.id
        if (!id) {
            throw new IllegalStateException('simulation must be persistent before putting it on the queue')
        }
    }

    void cancel(UUID uuid) {
        synchronized (lock) {
            def entry = new QueueEntry(uuid)
            queue.remove(entry)
            notifyRemoved(uuid)
            if (currentTask && currentTask?.entry?.id == uuid) {
                currentTask.entry.simulationTask.cancel()
                currentTask.gridTaskFuture.cancel()
            }
        }
    }
}

