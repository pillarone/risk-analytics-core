package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.output.SingleValueResult

public interface ICollectingModeStrategy {

    //do not use a grails domain class here (performance)
    List<SingleValueResultPOJO> collect(PacketList results) throws Exception

    String getDisplayName(Locale locale)

    String getIdentifier()

    void setPacketCollector(PacketCollector packetCollector)

}
