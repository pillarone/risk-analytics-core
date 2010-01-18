package org.pillarone.riskanalytics.core.util

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter

class TestProbe implements ITransmitter {

    private boolean transmitted
    PacketList result

    Component sender
    PacketList source

    public TestProbe(Component theSender, String propertyName) {
        sender = theSender
        source = sender[propertyName]
        result = []
        sender.allOutputTransmitter << this
    }

    public void transmit() {
        result.addAll(source)
        transmitted = true
    }

    public Component getReceiver() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PacketList getTarget() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTransmitted() {
        return transmitted
    }

    public void setTransmitted(boolean newTransmitted) {
        transmitted = newTransmitted
    }

}