package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.output.SingleValueResult

public interface ICollectingModeStrategy {

    List<SingleValueResult> collect(PacketList results)

    String getDisplayName(Locale locale)

    String getIdentifier()

    void setPacketCollector(PacketCollector packetCollector)

}
