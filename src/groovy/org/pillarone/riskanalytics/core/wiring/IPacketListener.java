package org.pillarone.riskanalytics.core.wiring;

import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.packets.PacketList;

public interface IPacketListener {

    public void packetSent(Transmitter t, PacketList packets);

    public void initComponentCache(Model m);
}
