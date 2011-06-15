package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent

class TestDynamicComponent extends DynamicComposedComponent {

    PacketList input1 = new PacketList();

    PacketList outValue1 = new PacketList();


    Component createDefaultSubComponent() {
        return new TestComponent()
    }

    @Override
    void wire() {
        replicateInChannels this, 'input1'
        replicateOutChannels this, 'outValue1'
    }
}
