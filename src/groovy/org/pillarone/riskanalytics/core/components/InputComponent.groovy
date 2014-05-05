package org.pillarone.riskanalytics.core.components

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.packets.ExternalPacket
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

@CompileStatic
abstract class InputComponent extends Component {

    PacketList<ExternalPacket> outData = []
    SimulationScope simulationScope


    abstract DataSourceDefinition getDefinition()

    @Override
    protected final void doCalculation() {
        List<ExternalPacket> externalValues = simulationScope.resultDataSource.getValuesForDefinition(getDefinition())
        if(externalValues == null) {
            throw new IllegalStateException("No data found for ${definition}")
        }
        outData.addAll(externalValues)
    }
}
