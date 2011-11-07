package org.pillarone.riskanalytics.core.output.aggregation

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.Packet


interface IPacketAggregator<E extends Packet> {

    E aggregate(PacketList<E> packetList)
}
