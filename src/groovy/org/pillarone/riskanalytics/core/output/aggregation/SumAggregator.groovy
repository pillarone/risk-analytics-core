package org.pillarone.riskanalytics.core.output.aggregation

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.packets.Packet

@CompileStatic
class SumAggregator implements IPacketAggregator<Packet> {

    Packet aggregate(List<Packet> packets) {

        Packet aggregatedPacket = (Packet) packets[0].class.newInstance()
        Map<String, Number> packetValues = [:]
        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            Map<String, Number> valuesToAggregate = p.getValuesToSave();
            for (Map.Entry<String, Number> entry: valuesToAggregate.entrySet()) {
                if (packetValues.containsKey(entry.getKey())) {
                    packetValues.put(entry.getKey(), ((Double) packetValues.get(entry.getKey())) + ((Double) entry.getValue()))
                } else {
                    packetValues.put(entry.getKey(), entry.getValue())
                }
            }
        }

        for (Map.Entry<String, Number> entry in packetValues.entrySet()) {
            aggregatedPacket[entry.key] = entry.value
        }

        return aggregatedPacket
    }


}
