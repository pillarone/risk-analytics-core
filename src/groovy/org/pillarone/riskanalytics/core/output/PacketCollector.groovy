package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert
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

    private PathMapping pathMapping
    private CollectorMapping collectorMapping
    private Map fieldMappings = [:]

    String path
    String collectorName = AbstractBulkInsert.DEFAULT_COLLECTOR_NAME

    public PacketCollector() { }

    public PacketCollector(ICollectingModeStrategy mode) {
        this.mode = mode
        this.mode.packetCollector = this
    }

    protected void doCalculation() {
        if (inPackets.empty) {return}

        outputStrategy << mode.collect(inPackets)
    }

    //Do not rename this method to getPathMapping, otherwise it will be called from getProperties which has a performance impact

    PathMapping pathMapping() {
        if (!pathMapping) {
            pathMapping = PathMapping.findByPathName(path)
            if (!pathMapping) {
                pathMapping = new PathMapping(pathName: path)
                assert pathMapping.save()
            }
        }
        return pathMapping
    }
    //Do not rename this method to getCollectorMapping, otherwise it will be called from getProperties which has a performance impact

    CollectorMapping collectorMapping() {
        if (!collectorMapping) {
            collectorMapping = CollectorMapping.findByCollectorName(collectorName)
            if (!collectorMapping) {
                collectorMapping = new CollectorMapping(collectorName: collectorName)
                assert collectorMapping.save()
            }
        }
        return collectorMapping
    }

    FieldMapping getFieldMapping(String name) {

        def fieldMapping = fieldMappings[name]
        if (!fieldMapping) {
            fieldMapping = FieldMapping.findByFieldName(name)
            if (!fieldMapping) {
                fieldMapping = new FieldMapping(fieldName: name)
                fieldMapping.save()
            }
            fieldMappings[name] = fieldMapping
        }
        return fieldMapping
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

}
