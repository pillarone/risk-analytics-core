package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.packets.PacketList

/**
 * Even if this code looks very similar to MultiPhaseComponent it's not!
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
abstract class DynamicMultiPhaseComposedComponent extends DynamicComposedComponent implements IChannelAllocation {

    private Map<ITransmitter, String> phasePerTransmitterInput = new HashMap<ITransmitter, String>()
    private Map<ITransmitter, String> phasePerTransmitterOutput = new HashMap<ITransmitter, String>()
    private Map<String, List<ITransmitter>> replicationInputTransmitterPerPhase = new HashMap<String, List<ITransmitter>>()
    private Map<String, Integer> numberOfTransmitterPerPhaseInput = new HashMap<String, Integer>()
    private Map<String, Integer> numberOfTransmitterPerPhaseOutput = new HashMap<String, Integer>()
    private Map<String, Integer> numberOfNotifiedTransmittersPerPhase = new HashMap<String, Integer>()
    private Integer numberOfPhases;
    private int finishedPhases;

    /**
     * Executed once only per instance. Calls allocateChannelsToPhases() and initialized numberOfNotifiedTransmittersPerPhase
     */
    private void init() {
        if (numberOfPhases) return
        for (String phase : numberOfTransmitterPerPhaseInput.keySet()) {
            numberOfNotifiedTransmittersPerPhase.put(phase, 0)
        }
        numberOfPhases = Math.max(numberOfTransmitterPerPhaseOutput.size(), numberOfTransmitterPerPhaseInput.size());
    }

    /**
     * Nothing happens as doCalculation(phase) is used in the context of a MultiPhaseComponent.
     * Replicating transmitters are fire ...
     */
    @Override
    protected final void doCalculation() {
    }

    /**
     * @param phase
     */
    protected final void calculateAndPublish(String phase){
        init()
        doCalculation(phase)
        publishResults(phase)
    }

    abstract protected void doCalculation(String phase);

    /**
     * Execute doCalculation of all phases
     */
    @Override
    protected void execute() {
        init()
        for (String phase : numberOfTransmitterPerPhaseOutput.keySet()) {
            calculateAndPublish(phase)
        }
        reset()
    }

    @Override
    public void notifyTransmitted(ITransmitter transmitter) {
        init();
        String transmitterPhase = phasePerTransmitterInput.get(transmitter)
        increaseNumberOfTransmitter(numberOfNotifiedTransmittersPerPhase, transmitterPhase)
        if (numberOfNotifiedTransmittersPerPhase[transmitterPhase] == numberOfTransmitterPerPhaseInput[transmitterPhase]) {
            for (ITransmitter replicationTransmitter : replicationInputTransmitterPerPhase.get(transmitterPhase)) {
                replicationTransmitter.transmit()
            }
            publishResults transmitterPhase
        }
    }

    /**
     * @param packetList adds the corresponding transmitter as key to the phasePerTransmitterOutput map
     * @param phase used as corresponding value and stored within phasePerTransmitterOutput
     */
    public void setTransmitterPhaseOutput(PacketList packetList, String phase) {
        if (isSenderWired(packetList)) {
            for (ITransmitter transmitter : getAllOutputTransmitter()) {
                if (transmitter.getSource().is(packetList)) {
                    phasePerTransmitterOutput.put(transmitter, phase)
                    increaseNumberOfTransmitter(numberOfTransmitterPerPhaseOutput, phase)
                }
            }
        }
    }

    /**
     * @param packetList adds the corresponding transmitter as key to the phasePerTransmitterInput map
     * @param phase used as corresponding value and stored within phasePerTransmitterInput
     */
    public void setTransmitterPhaseInput(PacketList packetList, String phase) {
        if (isReceiverWired(packetList)) {
            for (ITransmitter transmitter : getAllInputTransmitter()) {
                if (transmitter.getTarget().is(packetList)) {
                    phasePerTransmitterInput.put(transmitter, phase)
                    increaseNumberOfTransmitter(numberOfTransmitterPerPhaseInput, phase)

                    List<ITransmitter> replicationTransmitters = replicationInputTransmitterPerPhase.get(phase)
                    if (!replicationTransmitters) {
                        replicationTransmitters = new ArrayList<ITransmitter>()
                        replicationInputTransmitterPerPhase.put(phase, replicationTransmitters)
                    }
                    for (ITransmitter replicationTransmitter : getAllInputReplicationTransmitter()) {
                        if (transmitter.getTarget().is(replicationTransmitter.getSource())) {
                            replicationTransmitters.add(replicationTransmitter)
                        }
                    }
                }
            }
        }
    }

    /**
     * @param map key: phase, value: number of transmitters
     * @param phase the value of the corresponding phase will be increased by 1
     */
    private void increaseNumberOfTransmitter(Map<String, Integer> map, String phase) {
        Integer numberOfTransmitter = map.get(phase)
        map.put(phase, numberOfTransmitter == null ? 1 : numberOfTransmitter + 1)
    }

    /**
     * nothing happens as publishResults(phase) is used in the context of a multi phase component
     */
    protected void publishResults() {
    }
    /**
     * Publish results of transmitters belonging to the phase and call reset if all phases have been executed
     * @param phase
     */
    protected void publishResults(String phase) {
        for (Map.Entry<ITransmitter, String> entry : phasePerTransmitterOutput.entrySet()) {
            if (entry.getValue().equals(phase)) {
                entry.getKey().transmit()
            }
        }
        finishedPhases++;
        if (finishedPhases == numberOfPhases) {
            reset()
        }
    }

    @Override
    protected void reset() {
        super.reset()
        finishedPhases = 0
        numberOfNotifiedTransmittersPerPhase.clear()
    }
}
