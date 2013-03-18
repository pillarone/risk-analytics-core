package org.pillarone.riskanalytics.core.output.aggregation

import org.pillarone.riskanalytics.core.packets.Packet

interface IPacketAggregator<E extends Packet> {

    E aggregate(List<E> packetList)
}
