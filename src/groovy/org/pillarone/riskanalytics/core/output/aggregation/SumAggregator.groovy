package org.pillarone.riskanalytics.core.output.aggregation

import org.pillarone.riskanalytics.core.packets.Packet

class SumAggregator implements IPacketAggregator<Packet> {

    Packet aggregate(List<Packet> packets) {

        Packet aggregatedPacket = packets[0].class.newInstance()
        Map<String, Number> packetValues = [:]
        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            Map valuesToAggregate = p.getValuesToSave();
            Set<Map.Entry> entrySet = valuesToAggregate.entrySet();
            for (Map.Entry entry: entrySet) {
                if (packetValues.containsKey(entry.getKey())) {
                    packetValues.put(entry.getKey(), ((Double) packetValues.get(entry.getKey())) + ((Double) entry.getValue()))
                } else {
                    packetValues.put(entry.getKey(), entry.getValue())
                }
            }
        }

        for (Map.Entry<String, Number> entry in packetValues) {
            aggregatedPacket[entry.key] = entry.value
        }

        return aggregatedPacket
    }


}
