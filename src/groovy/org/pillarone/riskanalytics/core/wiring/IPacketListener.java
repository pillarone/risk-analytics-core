package org.pillarone.riskanalytics.core.wiring;

import org.pillarone.riskanalytics.core.model.Model;

public interface IPacketListener {

    public void packetSent(Transmitter t);

    public void initComponentCache(Model m);
}
