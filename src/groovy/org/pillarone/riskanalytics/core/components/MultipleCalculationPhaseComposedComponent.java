package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.ITransmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * A multiple entry composed component has the ability to set out properties
 * in different steps.
 *
 * @author: stefan.kunz (at) intuitive-collaboration (dot) com
 */
abstract public class MultipleCalculationPhaseComposedComponent extends ComposedComponent implements IChannelAllocation {

    private Map<ITransmitter, String> phaseTransmitterInput = new HashMap<ITransmitter, String>();
    private Map<ITransmitter, String> phaseTransmitterOutput = new HashMap<ITransmitter, String>();

    protected static final String PHASE_START = "start";
    protected static final String PHASE_DO_CALCULATION = "calculation";

    public void start() {
        transmitIncomingStartPhasePackets();
        startCalculation();
        publishStartCalculationResults();
        resetStartCalculationTransmitters();
    }

    /**
     * Trigger transmitters belonging to the start phase in order to send packets to sub components
     */
    private void transmitIncomingStartPhasePackets() {
        for (ITransmitter transmitter : getAllInputReplicationTransmitter()) {
            if (belongsToPhaseStart(transmitter)) {
                transmitter.transmit();
            }
        }
    }

    protected void startCalculation() {
    }

    protected void doCalculation() {
        for (ITransmitter transmitter : getAllInputReplicationTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterInput.get(transmitter);
            if (PHASE_DO_CALCULATION.equals(phaseOfTransmitter)) {
                transmitter.transmit();
            }
        }
    }

    public void setTransmitterPhaseOutput(PacketList packetList, String phase) {
        for (ITransmitter transmitter : getAllOutputTransmitter()) {
            if (transmitter.getSource() == (packetList)) {
                phaseTransmitterOutput.put(transmitter, phase);
            }
        }
    }

    public void setTransmitterPhaseInput(PacketList packetList, String phase) {
        for (ITransmitter transmitter : getAllInputReplicationTransmitter()) {
            if (transmitter.getSource() == (packetList)) {
                phaseTransmitterInput.put(transmitter, phase);
            }
        }
    }

    protected void setTransmitterPhase(ITransmitter transmitter, String phase) {
        phaseTransmitterOutput.put(transmitter, phase);
    }

    /**
     * publish on results of transmitters belonging to the DO_CALCULATION_PHASE
     */
    protected void publishResults() {
        for (ITransmitter output : getAllOutputTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterOutput.get(output);
            if (!PHASE_START.equals(phaseOfTransmitter)) {  // this criteria makes sure that CopyTransmitters wired for Collectors will transmit too.
                output.transmit();
            }
        }
    }

    /**
     * publish on results of transmitters belonging to the START_PHASE
     */
    private void publishStartCalculationResults() {
        for (ITransmitter output : getAllOutputTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterOutput.get(output);
            if (PHASE_START.equals(phaseOfTransmitter)) {
                output.transmit();
            }
        }
    }

    private void resetStartCalculationTransmitters() {
        for (ITransmitter output : getAllOutputTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterOutput.get(output);
            if (PHASE_START.equals(phaseOfTransmitter)) {
                output.setTransmitted(false);
            }
        }
    }

    public boolean belongsToPhaseStart(ITransmitter transmitter) {
        return (PHASE_START.equals(phaseTransmitterInput.get(transmitter)));
    }

    public boolean belongsToPhaseCalculation(ITransmitter transmitter) {
        return (PHASE_DO_CALCULATION.equals(phaseTransmitterInput.get(transmitter)));
    }

    protected void reset() {
        super.reset();
    }
}