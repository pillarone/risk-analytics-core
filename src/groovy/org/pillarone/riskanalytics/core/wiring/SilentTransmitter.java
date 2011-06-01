package org.pillarone.riskanalytics.core.wiring;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.packets.PacketList;

/**
 * A SilentTransmitter does not notify any receiver as it is used only
 * to relay output channels of subcomponents to output channels of their
 * surrounding ComposedComponent.
 */
public class SilentTransmitter extends Transmitter {

    public SilentTransmitter(Component sender, PacketList source, Component receiver, PacketList target) {
        super(sender, source, receiver, target);
    }

    protected void notifyReceiver() {
        // be silent
    }

    protected void notifyReceiver(Transmitter transmitter) {
        // be silent
    }


}