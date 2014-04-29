package org.pillarone.riskanalytics.core.packets

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.components.DataSourceDefinition

@CompileStatic
abstract class ExternalPacket extends Packet {

    DataSourceDefinition basedOn

    String field
    int iteration
    int period
}
