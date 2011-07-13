package org.pillarone.riskanalytics.core.wiring;

import org.pillarone.riskanalytics.core.components.ComposedComponent;
import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;

import java.util.UUID;

public class TraceableTransmitter extends Transmitter {

    private Transmitter transmitter;
    private IPacketListener packetListener;


    public TraceableTransmitter(Transmitter transmitter, IPacketListener packetListener) {
        super(transmitter.getSender(), transmitter.getSource(), transmitter.getReceiver(), transmitter.getTarget());
        this.transmitter = transmitter;
        this.packetListener = packetListener;
    }

    public void transmit() {
        if (isTransmitted()) { // TODO (msh): check case of retransmission and delete flag if unneccessary
            throw new IllegalStateException("No retransmission allowed: " + this);
        }

        if (source.isEmpty() && !ComposedComponent.class.isAssignableFrom(sender.getClass())) {
            try {
                Packet p=(Packet) source.getType().newInstance();
                p.initDefaultPacket();
                p.setOrigin(sender);
                source.add(p);
            } catch (Exception e) {
                
            }
        }

        setMarkers();
        PacketList targetClone = (PacketList) target.clone();
        filterSource();

        PacketList filteredPackets = ((PacketList) target.clone());
        filteredPackets.removeAll(targetClone);

        if (!ComposedComponent.class.isAssignableFrom(sender.getClass())) {

            for (Object o : filteredPackets) {
                if (((Packet) o).id == null)
                    ((Packet) o).id = UUID.randomUUID();
            }
        }

        packetListener.packetSent(this, filteredPackets);

        setTransmitted(true);
        notifyReceiver(this);
    }

    protected void notifyReceiver(Transmitter transmitter) {
        this.transmitter.notifyReceiver(transmitter);
    }

    public boolean isTransmitted() {
        return transmitter.isTransmitted();
    }

    public void setTransmitted(boolean transmittedValue) {
        transmitter.setTransmitted(transmittedValue);
    }


}
