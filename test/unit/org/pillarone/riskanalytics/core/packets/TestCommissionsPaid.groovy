package org.pillarone.riskanalytics.core.packets

import org.pillarone.riskanalytics.core.packets.MultiValuePacket

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class TestCommissionsPaid extends MultiValuePacket {

    public double total;
    public double acquisition;
    public double portfolio;
    public double nominalClawback;
    public double clawbackShortfall;

    
}
