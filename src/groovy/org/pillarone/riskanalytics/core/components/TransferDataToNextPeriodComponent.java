package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.wiring.ITransmitter;

/**
 * This component has only to be executed as a start component. Therefore it has to belong
 * to the start components of a model. Furthermore the component is not reseted at the
 * end of a period.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
abstract public class TransferDataToNextPeriodComponent extends Component {

    protected void execute() {
        doCalculation();
        resetInputTransmitters();
        resetInChannels();
        publishResults();
        resetOutChannels();
    }

    /**
     * avoid execute() after all channels have received their packets
     */
    public void notifyTransmitted(ITransmitter transmitter) {
    }

    protected void reset() {
    }
}
