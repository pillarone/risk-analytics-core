package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.packets.PacketList;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public interface IChannelAllocation {
    /**
     * This method is used to assign each channel to a specific phase
     */
    void allocateChannelsToPhases();

    void setTransmitterPhaseOutput(PacketList packetList, String phase);

    void setTransmitterPhaseInput(PacketList packetList, String phase);
}
