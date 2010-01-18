package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
abstract public class MultiPhaseDynamicComposedComponent extends DynamicComposedComponent {

    private Map<ITransmitter, String> phaseTransmitterInput = new HashMap<ITransmitter, String>();
    private Map<ITransmitter, String> phaseTransmitterOutput = new HashMap<ITransmitter, String>();
    private int startTransmitterCount;
    private int calculationTransmitterCount;
    private int startTransmitterNumber;
    private int calculationTransmitterNumber;

    // todo(sku): replace with enum
    public static final String PHASE_START = "start";
    public static final String PHASE_DO_CALCULATION = "calculation";

    abstract public void allocateChannelsToPhases()

    ;

    public void notifyTransmitted(ITransmitter transmitter) {
        String phaseOfTransmitter = "";
        if (allInputTransmitter.contains(transmitter)) {
            phaseOfTransmitter = phaseTransmitterInput.get(getReplicateInputTransmitter(transmitter))
        }
        else { // transmitter is a replicate transmitter
            phaseOfTransmitter = phaseTransmitterInput.get(transmitter);
        }
        if (PHASE_START.equals(phaseOfTransmitter)) {
            super.notifyTransmitted(transmitter);
            if (++startTransmitterCount == startTransmitterNumber) {
                start();
            }
        }
        else if (PHASE_DO_CALCULATION.equals(phaseOfTransmitter)) {
            //if (++calculationTransmitterCount == calculationTransmitterNumber) {
            super.notifyTransmitted(transmitter);
            //}
        }
        else {
            throw new IllegalArgumentException("Unknown phase: " + phaseOfTransmitter);
        }
    }

    private ITransmitter getReplicateInputTransmitter(ITransmitter transmitter) {
        for (ITransmitter replicateTransmitter: allInputReplicationTransmitter) {
            if (replicateTransmitter.getSource().is(transmitter.getTarget())) {
                return replicateTransmitter;
            }
        }
        return null;
    }

    protected void resetInputTransmitters() {
        startTransmitterCount = 0;
        calculationTransmitterCount = 0;
        super.resetInputTransmitters();
    }

    public void start() {
        transmitIncomingStartPhasePackets();
        startCalculation();
        publishStartCalculationResults();
        resetStartCalculationTransmitters();
    }

    /**
     *  Trigger transmitters belonging to the start phase in order to send packets to sub components
     */
    private void transmitIncomingStartPhasePackets() {
        for (ITransmitter transmitter: getAllInputReplicationTransmitter()) {
            if (belongsToPhaseStart(transmitter)) {
                transmitter.transmit();
            }
        }
    }

    protected void startCalculation() {
        for (Component component: componentList) {
            component.start()
        }
    }

    protected void doCalculation() {
        for (ITransmitter transmitter: getAllInputReplicationTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterInput.get(transmitter);
            if (PHASE_DO_CALCULATION.equals(phaseOfTransmitter)) {
                transmitter.transmit();
            }
        }
    }

    public void setTransmitterPhaseOutput(PacketList packetList, String phase) {
        for (ITransmitter transmitter: getAllOutputTransmitter()) {
            if (transmitter.getSource().is(packetList)) {
                phaseTransmitterOutput.put(transmitter, phase);
            }
        }
    }

    public void setTransmitterPhaseInput(PacketList packetList, String phase) {
        boolean packetListIsSourceOfTransmitter = false;
        for (ITransmitter transmitter: getAllInputReplicationTransmitter()) {
            if (transmitter.getSource().is(packetList)) {
                packetListIsSourceOfTransmitter = true;
                phaseTransmitterInput.put(transmitter, phase);
            }
        }
        if (packetListIsSourceOfTransmitter) {
            if (PHASE_DO_CALCULATION.equals(phase)) {
                calculationTransmitterNumber++
            }
            else if (PHASE_START.equals(phase)) {
                startTransmitterNumber++
            }
        }
    }

/*    protected void setTransmitterPhase(ITransmitter transmitter, String phase) {
        phaseTransmitterOutput.put(transmitter, phase);
    }*/

    /** publish on results of transmitters belonging to the DO_CALCULATION_PHASE      */
    protected void publishResults() {
        for (ITransmitter output: getAllOutputTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterOutput.get(output);
            if (!PHASE_START.equals(phaseOfTransmitter)) {  // this criteria makes sure that CopyTransmitters wired for Collectors will transmit too.
                output.transmit();
            }
        }
    }

    /** publish on results of transmitters belonging to the START_PHASE      */
    private void publishStartCalculationResults() {
        for (ITransmitter output: getAllOutputTransmitter()) {
            String phaseOfTransmitter = phaseTransmitterOutput.get(output);
            if (PHASE_START.equals(phaseOfTransmitter)) {
                output.transmit();
            }
        }
    }

    private void resetStartCalculationTransmitters() {
        for (ITransmitter output: getAllOutputTransmitter()) {
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
}