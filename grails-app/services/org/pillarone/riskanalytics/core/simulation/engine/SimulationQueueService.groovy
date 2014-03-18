package org.pillarone.riskanalytics.core.simulation.engine

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.gridgain.grid.Grid
import org.gridgain.grid.GridFuture
import org.gridgain.grid.GridTaskFuture
import org.gridgain.grid.lang.GridInClosure
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
    private GridInClosure<GridFuture<Object>> closure
    Grid grid

    @PostConstruct
    void initialize() {
        closure = new GridInClosure<GridFuture<Object>>() {
            @Override
            void apply(GridFuture<Object> objectGridFuture) {
                synchronized (lock) {
                    currentTask.gridTaskFuture.stopListenAsync(closure)
                    handlers.remove(currentTask.simulationHandler)
                    currentTask = null
                    //TODO remove from map
                }
                poll()
            }
        }
        //TODO collect entries from db to populate queue

    }

    private void poll() {
        synchronized (lock) {
            QueueEntry queueEntry = queue.poll()
            if (queueEntry) {
                handlers.inverse().remove(queueEntry)
                SimulationHandler simulationHandler = handlers.inverse()[queueEntry]
                GridTaskFuture gridTaskFuture = grid.execute(queueEntry.simulationTask, queueEntry.simulationConfiguration)
                gridTaskFuture.listenAsync(closure)
                currentTask = new CurrentTask(gridTaskFuture: gridTaskFuture, simulationHandler: simulationHandler)
            }
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
        Long id = configuration?.simulation?.id
        if (!id) {
            throw new IllegalStateException('simulation must be persistent before putting it on the queue')
        }
        //maybe we can do this immediately before we move it to the grid
        configuration.createMappingCache(configuration.simulation.template)
        configuration.prepareSimulationForGrid()
        configuration.beans = SpringBeanDefinitionRegistry.requiredBeanDefinitions
        QueueEntry queueEntry = new QueueEntry(
                priority: priority,
                simulationTask: new SimulationTask(),
                simulationConfiguration: configuration
        )
        def handler = new SimulationHandler(
                simulationTask: queueEntry.simulationTask
        )

        synchronized (lock) {
            handlers[handler] = queueEntry
            queue.offer(queueEntry)
            if (!currentTask) {
                poll()
            }
        }
        handler
    }

    void cancel(SimulationHandler handler) {
        synchronized (lock) {
            handlers.remove(handler)
            QueueEntry entry = handlers[handler]
            if (entry) {
                entry.simulationTask.cancel()
                queue.remove(entry)
            }
            if (currentTask.simulationHandler == handler) {
                def future = currentTask.gridTaskFuture
                future.stopListenAsync(closure)
                future.cancel()
                currentTask = null
                poll()
            }
        }
    }
}

