package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.components.InitializingComponent
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class ExampleInputOutputComponent extends Component implements InitializingComponent {

    PacketList<Packet> outValue = new PacketList<Packet>()
    PacketList<Packet> inValue = new PacketList<Packet>()
    ExampleParameterObject parmParameterObject = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])

    def injectedScope

    int globalInt
    String globalString

    protected void doCalculation() {

    }

    void afterParameterInjection(SimulationScope scope) {
        injectedScope = scope
    }


}