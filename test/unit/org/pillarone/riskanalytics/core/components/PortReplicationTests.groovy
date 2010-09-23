package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

class PortReplicationTests extends GroovyTestCase {

    int iterations = 10000
    ComposedRelayComponent composedComponent
    RelayComponent triggerComponent
    RelayComponent subComponent

    protected void setUp() {
        super.setUp()
        composedComponent = new ComposedRelayComponent(replicatedWiring: true)
        triggerComponent = new RelayComponent()
        subComponent = new RelayComponent()
    }

    void testNothing() {

    }

    // todo(sku): fix with mhu (Components to be wired must be properties of the callee!. Expression: componentFound. Values: componentFound = false)
/*    void testComposedComponent() {
        composedComponent.wire()

        WiringUtils.use(WireCategory) {
            composedComponent.inPut = triggerComponent.outPut
        }

        triggerComponent.outPut << new Frequency(value: 1)
        long start = System.currentTimeMillis()
        iterations.times {
            triggerComponent.start()
        }
        long end = System.currentTimeMillis()

        println "composed duration: ${end - start}"
    }

    void testDirectWiring() {
        composedComponent.wire()

        WiringUtils.use(WireCategory) {
            composedComponent.subComponent.inPut = triggerComponent.outPut
        }

        triggerComponent.outPut << new Frequency(value: 1)
        long start = System.currentTimeMillis()
        iterations.times {
            triggerComponent.start()
        }
        long end = System.currentTimeMillis()

        println "direct duration: ${end - start}"
    }*/
}

class ComposedRelayComponent extends ComposedComponent {

    boolean replicatedWiring

    PacketList inPut = new PacketList()
    PacketList outPut = new PacketList()

    public void wire() {

        if (replicatedWiring) {

            WiringUtils.use(PortReplicatorCategory) {
                this.outPut = subComponent.outPut
            }

            WiringUtils.use(PortReplicatorCategory) {
                subComponent.inPut = this.inPut
            }
        }
    }

}

class RelayComponent extends Component {

    PacketList inPut = new PacketList()
    PacketList outPut = new PacketList()

    protected void doCalculation() {
        outPut.addAll(inPut)
    }

    public void reset() {
        super.reset();
        allOutputTransmitter.each {it.transmitted = false}
    }
}
