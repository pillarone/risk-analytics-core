package org.pillarone.riskanalytics.core.wiring;

import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.packets.PacketList;

/**
 * Listener which can be applied to a simulation to listen on sent packets.
 */
public interface IPacketListener {

    /**
     * Notify listener
     * @param t currently used transmitter
     * @param packets sent packets
     */
    public void packetSent(Transmitter t, PacketList packets);

    /**
     * Initialize component cache (resolve all components/subcomponents used in model)
     * @param m
     */
    public void initComponentCache(Model m);
}
