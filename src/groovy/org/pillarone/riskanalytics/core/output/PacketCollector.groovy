package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.RiskAnalyticsInconsistencyException
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.wiring.WireCategory

/**
 * A PacketCollector is a special component used for collecting other components output.
 * The PacketCollector converts all collected packets into SingleValueResult objects and
 * hands them over to the outputStrategy for persistence.
 * The mode property is used to control, whether the collected packets have to be aggregated before
 * passing them to the outputStrategy
 *
 * Attention: As the PacketCollector instance contains information about the path and field it collects, it can only be used for
 * collecting packets from a single outputChannel.
 *
 * The PacketCollector is able to attach itself to the models component specified by the path.
 */

public class PacketCollector extends Component {

    ICollectingModeStrategy mode

    ICollectorOutputStrategy outputStrategy

    PacketList<Packet> inPackets = new PacketList(Packet)

    SimulationScope simulationScope

    /**
     * globalSanityChecks is injected by the framework, but defaults to true. The modeller should provide a global variable
     * which is set by the user on the run screen. This variable decides whether or not to halt the simulation should
     * an insanity (NaN, infinity) etc be collected.
     */
    boolean globalSanityChecks = true

    String path

    public PacketCollector() { }

    public PacketCollector(ICollectingModeStrategy mode) {
        this.mode = mode
        this.mode.packetCollector = this
    }

    protected void doCalculation() {
        if (inPackets.empty) {return}
        outputStrategy << mode.collect(inPackets, globalSanityChecks)
    }

    @Override
    String getName() {
        return "Packet collector for path : " + path
    }

    @Override
    public final void setName(String name) {
        throw new RiskAnalyticsInconsistencyException("""The name of the packet collector cannot be set directly.
            It is  defined as a function of it's path.
        """)
    }
/**
     * Find the component matching the path information and wire to its outputChannel.
     */
    public attachToModel(Model model, StructureInformation structureInformation) {
        def pathElements = path.split("\\:")

        String modelName = model.class.simpleName
        if (modelName.endsWith("Model")) {
            modelName = modelName - "Model"
        }

        if (modelName != pathElements[0]) {
            throw new IllegalArgumentException("Model ${model.class.simpleName} does not match collector configuration: ${pathElements[0]}")
        }
        def component = model
        def outChannel = pathElements[-1]
        pathElements[1..-2].each {propertyName ->
            if (component.properties.containsKey(propertyName)) {
                component = component[propertyName]
            } else {
                component = structureInformation.componentPaths.inverse().get(pathElements[0..-2].join(":"))
            }
        }

        // TODO (Oct 8, 2009, msh): maybe set path of collector here. Also possible to determine fields (use PacketList.type.newInstance to create a packet

        WireCategory.doSetProperty(this, "inPackets", WireCategory.doGetProperty(component, outChannel))
    }

    @Override
    String toString() {
        return "$path ${mode?.identifier}"
    }
}
