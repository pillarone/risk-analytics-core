package org.pillarone.riskanalytics.core.output.aggregation;

import org.pillarone.riskanalytics.core.packets.SingleValuePacket;

import java.util.List;

public class SumAggregatorSingleValuePacket implements IPacketAggregator<SingleValuePacket> {

    public SingleValuePacket aggregate(List<SingleValuePacket> packets) {

        SingleValuePacket aggregatedPacket = null;
        try {
            aggregatedPacket = (SingleValuePacket) packets.get(0).getClass().newInstance();
            double aggregateValue = 0;
            for (Object packet : packets) {
                aggregateValue += ((SingleValuePacket) packet).getValue();
            }
            aggregatedPacket.setValue(aggregateValue);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return aggregatedPacket;
    }

}