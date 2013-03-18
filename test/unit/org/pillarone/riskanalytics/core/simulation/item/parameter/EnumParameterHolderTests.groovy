package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.EnumParameter


class EnumParameterHolderTests extends GroovyTestCase {

    //ART-618
    void testSaveLoad() {
        TestAbstractEnum aEnum = TestAbstractEnum.A
        assertTrue(aEnum.getClass() != aEnum.getDeclaringClass())

        EnumParameterHolder enumParameterHolder = new EnumParameterHolder("", 0, aEnum)
        final EnumParameter parameter = new EnumParameter()
        enumParameterHolder.applyToDomainObject(parameter)

        assertEquals("org.pillarone.riskanalytics.core.simulation.item.parameter.TestAbstractEnum", parameter.parameterType)
    }


}



