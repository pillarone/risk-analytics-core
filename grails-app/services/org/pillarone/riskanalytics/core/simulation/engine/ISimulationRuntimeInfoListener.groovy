package org.pillarone.riskanalytics.core.simulation.engine


interface ISimulationRuntimeInfoListener {
    void onEvent(SimulationRuntimeInfoEvent event)
}

abstract class SimulationRuntimeInfoEvent {
    SimulationRuntimeInfo info
}

class AddSimulationRuntimeInfoEvent extends SimulationRuntimeInfoEvent {
    int index
}

class ChangeSimulationRuntimeInfoEvent extends SimulationRuntimeInfoEvent {
}

class DeleteSimulationRuntimeInfoEvent extends SimulationRuntimeInfoEvent {
}