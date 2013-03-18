package org.pillarone.riskanalytics.core.packets


class MultiValuePacketTests extends GroovyTestCase {

    void testGetValues() {
        TestCommissionsPaid packet = new TestCommissionsPaid()
        packet.acquisition = 100
        packet.nominalClawback = 200
        packet.clawbackShortfall = 300
        packet.portfolio = 400
        packet.total = 500

        Map values = packet.valuesToSave
        assertEquals 5, values.size()

        assertEquals 100, values["acquisition"]
        assertEquals 200, values["nominalClawback"]
        assertEquals 300, values["clawbackShortfall"]
        assertEquals 400, values["portfolio"]
        assertEquals 500, values["total"]
    }

    void testMultiLevelInheritance() {
        TestMultiValuePacketDerivedTwice packet = new TestMultiValuePacketDerivedTwice()
        packet.firstLevelField = 1
        packet.secondLevelField = 2

        Map values = packet.valuesToSave
        assertEquals 'correct number of packets', 2, values.size()

        assertEquals 'first level', 1, values['firstLevelField']
        assertEquals 'second level', 2, values['secondLevelField']
    }
}
class TestMultiValuePacketDerivedTwice extends TestMultiValuePacketDerivedOnce {
    double secondLevelField
}