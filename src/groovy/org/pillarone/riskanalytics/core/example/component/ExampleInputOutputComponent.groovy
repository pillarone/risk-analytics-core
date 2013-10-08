package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.InitializingComponent
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import static junit.framework.Assert.*
import org.pillarone.riskanalytics.core.simulation.engine.id.IIdGenerator

class ExampleInputOutputComponent extends Component implements InitializingComponent {

    PacketList<Packet> outValue = new PacketList<Packet>()
    PacketList<Packet> inValue = new PacketList<Packet>()
    ExampleParameterObject parmParameterObject = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])
    ExampleParameterObject parmNewParameterObject = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])

    def injectedScope

    int globalInt
    String globalString

    Integer runtimeInt = 1

    boolean doCalculationCalled = false
    boolean afterParameterInjectionCalled = false

    protected void doCalculation() {
        println("called by ${System.identityHashCode(this)}: size: ${inValue.size()} in-transm: ${allInputTransmitter.size()}")
        IIdGenerator generator = getIdGenerator()
        assertNotNull(generator)
        String firstId = generator.nextValue()
        assertNotNull(firstId)
        assertFalse(firstId.equals(generator.nextValue()))
        doCalculationCalled = true
    }

    void afterParameterInjection(SimulationScope scope) {
        afterParameterInjectionCalled = true
        injectedScope = scope
        assertNotNull globalString
        assertEquals 1, globalInt
    }


}