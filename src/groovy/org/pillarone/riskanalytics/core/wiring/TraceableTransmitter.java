package org.pillarone.riskanalytics.core.wiring;

import org.pillarone.riskanalytics.core.components.ComposedComponent;
import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;

import java.util.UUID;

/**
 * Decorator of a Transmitter which notifies a PacketListener
 */
public class TraceableTransmitter extends Transmitter {

    private Transmitter transmitter;
    private IPacketListener packetListener;


    /**
     *
     * @param transmitter Transmitter to decorate
     * @param packetListener PacketListener to notify
     */
    public TraceableTransmitter(Transmitter transmitter, IPacketListener packetListener) {
        super(transmitter.getSender(), transmitter.getSource(), transmitter.getReceiver(), transmitter.getTarget());
        this.transmitter = transmitter;
        this.packetListener = packetListener;
    }

    public void transmit() {
        if (isTransmitted()) { // TODO (msh): check case of retransmission and delete flag if unneccessary
            throw new IllegalStateException("No retransmission allowed: " + this);
        }

        //Create default packets if no packets are sent on source channel
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
        PacketList targetBeforeFilter = (PacketList) target.clone();
        filterSource();

        PacketList targetAfterFilter = ((PacketList) target.clone());

        //Currently sent packets is difference between targetAfterFilter and targetBeforeFilter
        targetAfterFilter.removeAll(targetBeforeFilter);

        if (!ComposedComponent.class.isAssignableFrom(sender.getClass())) {

            for (Object o : targetAfterFilter) {
                if (((Packet) o).id == null)
                    ((Packet) o).id = UUID.randomUUID();
            }
        }

        packetListener.packetSent(this, targetAfterFilter);

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
