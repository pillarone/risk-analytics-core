package org.pillarone.riskanalytics.core.simulation.engine

import org.gridgain.grid.Grid
import org.gridgain.grid.GridTaskFuture
import org.pillarone.riskanalytics.core.queue.AbstractQueueService
import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement

class SimulationQueueService extends AbstractQueueService<SimulationConfiguration, SimulationQueueTaskContext, SimulationQueueEntry> {

    Grid grid

    @Override
    SimulationQueueEntry createQueueEntry(SimulationConfiguration configuration, int priority) {
        new SimulationQueueEntry(configuration, priority, currentUser)
    }

    @Override
    SimulationQueueEntry createQueueEntry(UUID id) {
        new SimulationQueueEntry(id)
    }

    @Override
    void preConditionCheck(SimulationConfiguration configuration) {
        //TODO discuss, what has to be fulfilled
        Long id = configuration?.simulation?.id
        if (!id) {
            throw new IllegalStateException('simulation must be persistent before putting it on the queue')
        }
    }

    @Override
    IQueueTaskFuture doWork(SimulationQueueTaskContext context, int priority) {
        GridTaskFuture future = grid.execute(context.simulationTask, context.simulationTask.simulationConfiguration)
        new SimulationQueueTaskFuture(future, context)
    }

    @Override
    void handleContext(SimulationQueueTaskContext context) {
        SimulationState simulationState = context.simulationTask.simulationState
        switch (simulationState) {
            case SimulationState.FINISHED:
            case SimulationState.ERROR:
            case SimulationState.CANCELED:
                break
            case SimulationState.NOT_RUNNING:
            case SimulationState.INITIALIZING:
            case SimulationState.RUNNING:
            case SimulationState.SAVING_RESULTS:
            case SimulationState.POST_SIMULATION_CALCULATIONS:
            default:
                log.error("task has finished, but state was $simulationState. This is likely to an internal gridgain error")
                context.simulationTask.simulationErrors.add(new Throwable("internal gridgain error"))
                context.simulationTask.simulationState = SimulationState.ERROR
        }
    }

    private static Person getCurrentUser() {
        UserManagement.currentUser
    }
}
