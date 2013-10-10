package org.pillarone.riskanalytics.core.output

import groovy.transform.CompileStatic
import org.jboss.serial.io.JBossObjectInputStream
import org.jboss.serial.io.JBossObjectOutputStream
import org.pillarone.riskanalytics.core.RiskAnalyticsInconsistencyException
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper
import org.pillarone.riskanalytics.core.util.GroovyUtils
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

    transient ICollectorOutputStrategy outputStrategy

    PacketList<Packet> inPackets = new PacketList(Packet)

    transient SimulationScope simulationScope

    /**
     * globalSanityChecks is injected by the framework, but defaults to true. The modeller should provide a global variable
     * which is set by the user on the run screen. This variable decides whether or not to halt the simulation should
     * an insanity (NaN, infinity) etc be collected.
     */
    boolean runtimeSanityChecks = true

    String path

    public PacketCollector() { }

    public void setMode(ICollectingModeStrategy mode) {
        this.mode = mode;
        this.mode.packetCollector = this;
    }

    public PacketCollector(ICollectingModeStrategy mode) {
        this.mode = mode
        this.mode.packetCollector = this
    }

    @CompileStatic
    protected void doCalculation() {
        if (inPackets.empty) {
            return
        }
        if (path == 'ORSA:segments:outClaimsCeded') {
            int period = simulationScope.iterationScope.periodScope.currentPeriod
            int iteration = simulationScope.iterationScope.currentIteration
            serialize()
//            deserialize(path, iteration, period, simulationScope.simulation.id as Long, Packet)
        }
        outputStrategy << mode.collect(inPackets, runtimeSanityChecks)
    }

    private static File getSerializePath(String path, int iteration, int period, long simulationId) {
        new File(GridHelper.getResultLocation(simulationId), "serial-${path}-${iteration}-${period}.ser")
    }

    static <E extends Packet> PacketList<E> deserialize(String path, int iteration, int period, long simulationId, E) {
        JBossObjectInputStream inStream = new JBossObjectInputStream(new FileInputStream(getSerializePath(path,iteration,period,simulationId)))
        PacketList<E> packetList = inStream.readObject() as PacketList<E>
        return packetList
    }

    private void serialize() {
        File serializePath = getSerializePath(path, simulationScope.iterationScope.currentIteration, simulationScope.iterationScope.periodScope.currentPeriod, simulationScope.simulation.id as Long)
        JBossObjectOutputStream stream = new JBossObjectOutputStream(new FileOutputStream(serializePath))
        stream.writeObject(inPackets)
        stream.close()
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
        pathElements[1..-2].each { propertyName ->
            if (GroovyUtils.getProperties(component).containsKey(propertyName)) {
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
