package org.pillarone.riskanalytics.core.simulation.engine

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.gridgain.grid.Grid
import org.gridgain.grid.GridTaskFuture
import org.gridgain.grid.typedef.CI1
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationHandler
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationTask
import org.pillarone.riskanalytics.core.simulation.engine.grid.SpringBeanDefinitionRegistry

import javax.annotation.PostConstruct

class SimulationQueueService {

    //TODO just to be backwards compatible. will be removed
    private final BiMap<SimulationHandler, QueueEntry> handlers = HashBiMap.create()
    private final PriorityQueue<QueueEntry> queue = new PriorityQueue<QueueEntry>()
    private CurrentTask currentTask
    private final Object lock = new Object()
    private CI1<GridTaskFuture> taskListener
    private boolean busy = false
    Grid grid


    @PostConstruct
    void initialize() {
        taskListener = new CI1<GridTaskFuture>() {
            @Override
            void apply(GridTaskFuture future) {
                synchronized (lock) {
                    busy = false
                    future.stopListenAsync(taskListener)
                    handlers.remove(currentTask.simulationHandler)
                    currentTask = null
                    poll()
                }
            }
        }
        //TODO collect entries from db to populate queue
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
                    startSimulation(queueEntry)
                }
            }
        }
    }

    private void startSimulation(QueueEntry queueEntry) {
        SimulationHandler simulationHandler = handlers.inverse()[queueEntry]
        SimulationConfiguration configuration = queueEntry.simulationConfiguration
        //TODO think about where to add the transaction
        SimulationRun.withTransaction {
            configuration.createMappingCache(configuration.simulation.template)
        }
        configuration.prepareSimulationForGrid()
        configuration.beans = SpringBeanDefinitionRegistry.requiredBeanDefinitions
        GridTaskFuture gridTaskFuture = grid.execute(queueEntry.simulationTask, queueEntry.simulationConfiguration)
        gridTaskFuture.listenAsync(taskListener)
        synchronized (lock) {
            currentTask = new CurrentTask(gridTaskFuture: gridTaskFuture, simulationHandler: simulationHandler)
        }
    }

    static class QueueEntry implements Comparable<QueueEntry> {
        int priority
        SimulationTask simulationTask
        SimulationConfiguration simulationConfiguration

        @Override
        int compareTo(QueueEntry o) {
            return priority.compareTo(o.priority)
        }
    }

    static class CurrentTask {
        GridTaskFuture gridTaskFuture
        SimulationHandler simulationHandler
    }

    public SimulationHandler offer(SimulationConfiguration configuration, int priority = 10) {
        preConditionCheck(configuration)

        QueueEntry queueEntry = new QueueEntry(
                priority: priority,
                simulationTask: new SimulationTask(),
                simulationConfiguration: configuration
        )
        SimulationHandler handler = new SimulationHandler(
                simulationTask: queueEntry.simulationTask
        )
        synchronized (lock) {
            handlers[handler] = queueEntry
            queue.offer(queueEntry)
        }
        poll()
        handler
    }

    private static void preConditionCheck(SimulationConfiguration configuration) {
        Long id = configuration?.simulation?.id
        if (!id) {
            throw new IllegalStateException('simulation must be persistent before putting it on the queue')
        }
    }

    void cancel(SimulationHandler handler) {
        synchronized (lock) {
            QueueEntry entry = handlers.remove(handler)
            if (entry) {
                entry.simulationTask.cancel()
                queue.remove(entry)
            }
            if (isCurrent(handler)) {
                currentTask.simulationHandler.simulationTask.cancel()
                currentTask.gridTaskFuture.cancel()
            }
        }
    }

    private boolean isCurrent(SimulationHandler handler) {
        currentTask?.simulationHandler == handler
    }
}

