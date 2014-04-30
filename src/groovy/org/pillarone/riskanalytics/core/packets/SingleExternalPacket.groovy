package org.pillarone.riskanalytics.core.packets

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.dataaccess.DateTimeValuePair

@CompileStatic
class SingleExternalPacket extends ExternalPacket {

    List<DateTimeValuePair> values
}
