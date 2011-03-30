package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.example.packet.TestPacket
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class RemoveParamMigrationComponent extends Component {

    PacketList<TestPacket> outResults1 = new PacketList<TestPacket>(TestPacket)
    PacketList<TestPacket> outResults2 = new PacketList<TestPacket>(TestPacket)
    PacketList<TestPacket> outResults3 = new PacketList<TestPacket>(TestPacket)

    TimeMode parmTimeMode = TimeMode.PERIOD
    IParameterObject parmStrategy = TestParameterObjectType.getDefault()

    @Override protected void doCalculation() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
