package org.pillarone.riskanalytics.core.example.component

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.components.ResourceHolder
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory
import org.pillarone.riskanalytics.core.example.parameter.ExampleResourceConstraints
import junit.framework.Assert


class ExampleComponentContainingResource extends Component {

    PacketList<SingleValuePacket> input = new PacketList<SingleValuePacket>(SingleValuePacket)
    PacketList<SingleValuePacket> output = new PacketList<SingleValuePacket>(SingleValuePacket)

    ResourceHolder<ExampleResource> parmResource = new ResourceHolder<ExampleResource>(ExampleResource, 'exampleResource', new VersionNumber('1'))
    ExampleParameterObject parmParameterObject = ExampleParameterObjectClassifier.RESOURCE.getParameterObject(["resource": new ConstrainedMultiDimensionalParameter([[new ResourceHolder(ExampleResource, "a", new VersionNumber("1"))]], ['title'], ConstraintsFactory.getConstraints(ExampleResourceConstraints.IDENTIFIER))])


    @Override
    protected void doCalculation() {
        for (SingleValuePacket inPacket : input) {
            output << new SingleValuePacket(inPacket.value * parmResource.resource.parmInteger)
        }

        Assert.assertSame(parmResource.resource, parmParameterObject.parameters["resource"].getValuesAsObjects(0)[0])
    }
}