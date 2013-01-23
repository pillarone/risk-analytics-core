package org.pillarone.riskanalytics.core.util

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter

// TODO(bgi): clarify/improve comment
/**
 * use TestPretendInChannelWired to pretend a component has an input wired
 */
class TestPretendInChannelWired implements ITransmitter {

    private boolean transmitted

    Component receiver
    PacketList target

    public TestPretendInChannelWired(Component theReceiver, String propertyName) {
        receiver = theReceiver
        target = receiver[propertyName]
        receiver.allInputTransmitter << this
    }

    public void transmit() {
        transmitted = true
    }

    public boolean isTransmitted() {
        return transmitted
    }

    public void setTransmitted(boolean newTransmitted) {
        transmitted = newTransmitted
    }

    Component getSender() {
        return null;
    }

    PacketList getSource() {
        return null;
    }
}
