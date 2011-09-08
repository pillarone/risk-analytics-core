package org.pillarone.riskanalytics.core.output.aggregation

import org.pillarone.riskanalytics.core.packets.PacketList


interface IPacketAggregator<E> {

    E aggregate(PacketList packetList)
}
