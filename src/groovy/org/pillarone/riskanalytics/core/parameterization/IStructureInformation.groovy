package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.Packet

interface IStructureInformation {

    public String getLine(Packet packet, String property)

    public String getLine(Packet packet)

    public String getLine(Component component)

    public String getPath(Packet packet)

    public String getComponentPath(Packet packet)

}