package org.pillarone.riskanalytics.core.model

import com.google.common.collect.Multimap
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.MarkerInterfaceCollector
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.components.IComponentMarker
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent

class ModelHelper {

    // todo(sku): try to reuse same constants of PC project
    private static final String PATH_SEPARATOR = ':'
    private static final String PERIOD = 'period'
    private static final String SPLIT = 'split'
    private static final String RESERVE_RISK_BASE = 'reserveRiskBase'
    private static final String PREMIUM_RISK_BASE = 'premiumRiskBase'
    private static final String PREMIUM_AND_RESERVE_RISK_BASE = "premiumAndReserveRiskBase";
    private static final String GROSS_RESERVE_RISK_BASE = 'grossReserveRiskBase'
    private static final String GROSS_PREMIUM_RISK_BASE = 'grossPremiumRiskBase'
    private static final String GROSS_PREMIUM_AND_RESERVE_RISK_BASE = "grossPremiumAndReserveRiskBase";
    private static final String NET_RESERVE_RISK_BASE = 'netReserveRiskBase'
    private static final String NET_PREMIUM_RISK_BASE = 'netPremiumRiskBase'
    private static final String NET_PREMIUM_AND_RESERVE_RISK_BASE = "netPremiumAndReserveRiskBase";
    private static final String CEDED_RESERVE_RISK_BASE = 'cededReserveRiskBase'
    private static final String CEDED_PREMIUM_RISK_BASE = 'cededPremiumRiskBase'
    private static final String CEDED_PREMIUM_AND_RESERVE_RISK_BASE = "cededPremiumAndReserveRiskBase";
    private static final String PERILS = "claimsGenerators"
    private static final String RESERVES = "reservesGenerators"
    private static final String CONTRACTS = "reinsuranceContracts"
    private static final String LOB = "linesOfBusiness"
    private static final String SEGMENTS = "segments"
    private static final String SEGMENT_MARKER = "SegmentMarker"
    private static final String PERIL_MARKER = "PerilMarker"
    private static final String RESERVE_MARKER = "IReserveMarker"
    private static final String CONTRACT_MARKER = "IReinsuranceContractMarker"
    private static final String LEGAL_ENTITY_MARKER = "ILegalEntityMarker"
    private static final String STRUCTURE_MARKER = "IStructureMarker"

    private static Log LOG = LogFactory.getLog(ModelHelper.class);

    /**
     * A static helper method which obtains all possible output paths of this model
     * @param model A model with all parameters injected
     * @return All possible fields
     */
    public static Set<String> getAllPossibleFields(Model model, boolean includePremiumReserveRisk) {
        Set<String> results = []
        model.properties.each { String key, value ->
            if (value instanceof Component) {
                results.addAll(getAllPossibleOutputFields(value))
            }
        }
        if (includePremiumReserveRisk) {
            results.add(RESERVE_RISK_BASE)
            results.add(PREMIUM_RISK_BASE)
            results.add(PREMIUM_AND_RESERVE_RISK_BASE)
            results.add(GROSS_RESERVE_RISK_BASE)
            results.add(GROSS_PREMIUM_RISK_BASE)
            results.add(GROSS_PREMIUM_AND_RESERVE_RISK_BASE)
            results.add(NET_RESERVE_RISK_BASE)
            results.add(NET_PREMIUM_RISK_BASE)
            results.add(NET_PREMIUM_AND_RESERVE_RISK_BASE)
            results.add(CEDED_RESERVE_RISK_BASE)
            results.add(CEDED_PREMIUM_RISK_BASE)
            results.add(CEDED_PREMIUM_AND_RESERVE_RISK_BASE)
        }
        return results
    }

    /**
     * A static helper method which obtains all possible output paths of this model
     * @param model A model with all parameters injected
     * @return All possible paths
     */
    public static Set<String> getAllPossibleOutputPaths(Model model, List<String> drillDownPaths) {
        String prefix = model.class.simpleName - "Model"
        Map<Class, List<String>> outputPathsByMarkerInterface = new HashMap<Class, List<String>>()
        Set<String> results = []

        MarkerInterfaceCollector collector = new MarkerInterfaceCollector()
        model.accept(collector)
        model.properties.each { String key, value ->
            if (value instanceof Component) {
                results.addAll(internalGetAllPossibleOutputPaths(prefix + ":${key}", value, collector, outputPathsByMarkerInterface, drillDownPaths))
            }
        }
        Multimap<Class, Component> componentsByMarkerInterface = collector.componentsByMarkerInterface()
        results.addAll getPossibleDrillDownOutputPaths(outputPathsByMarkerInterface, componentsByMarkerInterface)
        return injectStructure(results, ModelStructure.getStructureForModel(model.class))
    }

    private static Set<String> injectStructure(Set<String> results, ModelStructure structure) {
        structure.load()
        ConfigObject lines = structure.data.company
        if (lines.size() > 0) {
            Map repls = [:]
            lines.each { String line, data ->
                ConfigObject comps = data.components
                comps.each { String comp, val ->
                    repls.put(comp, "${line}:${comp}")
                }
            }
            Set res = []
            for (String path in results) {
                String newEntry = path
                for (Map.Entry entry in repls.entrySet()) {
                    if (path.contains(entry.key)) {
                        newEntry = path.replaceFirst(entry.key, entry.value)
                        break
                    }
                }
                res << newEntry
            }
            return res
        } else {
            return results
        }
    }

    private static Set<String> internalGetAllPossibleOutputPaths(String prefix, Component component,
                                                                 MarkerInterfaceCollector markerInterfaceCollector,
                                                                 Map<Class, List<String>> outputPathsByMarkerInterface,
                                                                 List<String> drillDownPaths) {
        Set<String> results = []
        component.properties.each { String key, value ->
            if (key.startsWith("out")) {
                String path = prefix + ":${key}"
                results.add path
                if ((drillDownPaths != null) && drillDownPaths.contains(path)) {
                    Set<Class> markerInterfaces = markerInterfaceCollector.getMarkerInterfaces(component)
                    findDrillDownCandidates markerInterfaces, path, outputPathsByMarkerInterface
                }
            } else if (value instanceof Component) {
                results.addAll(internalGetAllPossibleOutputPaths(prefix + ":${key}", value, markerInterfaceCollector, outputPathsByMarkerInterface, drillDownPaths))
            }
        }
        return results
    }

    private static void findDrillDownCandidates(Set<Class> componentMarkerInterfaces, String path,
                                                Map<Class, List<String>> outputPathsByMarkerInterface) {
        for (Class intf : componentMarkerInterfaces) {
            if (IComponentMarker.isAssignableFrom(intf)) {
                List components = outputPathsByMarkerInterface.get(intf)
                if (components) {
                    components.add(path)
                } else {
                    outputPathsByMarkerInterface.put(intf, [path])
                }
            }
        }
    }

//    /**
//     * @param component
//     * @return interfaces of the component or of its sub component if it is a DynamicComposedComponent
//     */
//    private static List<Class> getInterfaces(Component component) {
//        Set<Class> interfaces = []
//        if (component instanceof DynamicComposedComponent) {
//            Component subComponent = ((DynamicComposedComponent) component).createDefaultSubComponent()
//            getInterfaces(subComponent.class, interfaces)
//        }
//        else if (component instanceof ComposedComponent) {
//            getInterfaces(component.class, interfaces)
//            for(Component subComponent in component.allSubComponents()) {
//                getInterfaces(subComponent, interfaces)
//            }
//        }
//        else {
//            getInterfaces(component.class, interfaces)
//        }
//        List<Class> noneMarkerInterfaces = []
//        for (Class intf : interfaces) {
//            if (!IComponentMarker.isAssignableFrom(intf)) {
//                noneMarkerInterfaces.add(intf)
//            }
//        }
//        interfaces.removeAll(noneMarkerInterfaces)
//        return interfaces.toList()
//    }
//
//    private static void getInterfaces(Component component, Set<Class> interfaces) {
//        interfaces.addAll getInterfaces(component)
//    }
//
//    private static void getInterfaces(Class clazz, Set<Class> interfaces) {
//        interfaces.addAll clazz.interfaces
//        if (clazz.superclass != Component.class) {
//            getInterfaces(clazz.superclass, interfaces)
//        }
//    }

    /**
     * @param components
     * @param componentsByMarkerInterface this map is filled by traversing all components including nested and checking
     *                                       for every component if it implements a marker interface
     */
//    public static void collectComponentsByMarkerInterface(List<Component> components,
//                                                              Map<Class, List<Component>> componentsByMarkerInterface) {
//        for (Component component : components) {
//            for (Class intf : getInterfaces(component)) {
//                if (IComponentMarker.isAssignableFrom(intf)) {
//                    List<Component> componentsWithMarkerInterface = componentsByMarkerInterface.get(intf)
//                    if (componentsWithMarkerInterface == null) {
//                        componentsWithMarkerInterface = new ArrayList<Component>()
//                        componentsByMarkerInterface.put(intf, componentsWithMarkerInterface)
//                    }
//                    componentsWithMarkerInterface.add(component)
//                }
//            }
//            if (component instanceof ComposedComponent) {
//                collectComponentsByMarkerInterface component.allSubComponents(), componentsByMarkerInterface
//            }
//        }
//    }

    private static Set<String> getPossibleDrillDownOutputPaths(Map<Class, List<String>> outputPathsByMarkerInterface,
                                                               Multimap<Class, Component> componentsByMarkerInterface) {
        Set<String> results = []
        Class lobMarker = componentsByMarkerInterface.keySet().find { clazz -> clazz.name.contains(SEGMENT_MARKER) }
        Class perilMarker = componentsByMarkerInterface.keySet().find { clazz -> clazz.name.contains(PERIL_MARKER) }
        Class reserveMarker = componentsByMarkerInterface.keySet().find { clazz -> clazz.name.contains(RESERVE_MARKER) }
        Class contractMarker = componentsByMarkerInterface.keySet().find { clazz -> clazz.name.contains(CONTRACT_MARKER) }
        Class legalEntityMarker = componentsByMarkerInterface.keySet().find { clazz -> clazz.name.contains(LEGAL_ENTITY_MARKER) }
        Class structureMarker = componentsByMarkerInterface.keySet().find { clazz -> clazz.name.contains(STRUCTURE_MARKER) }

        for (String path : outputPathsByMarkerInterface.get(lobMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentsByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, reserveMarker, RESERVES, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, contractMarker, CONTRACTS, pathWithoutChannel, channel, results)
        }
        for (String path : outputPathsByMarkerInterface.get(contractMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentsByMarkerInterface, lobMarker, LOB, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, lobMarker, SEGMENTS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, reserveMarker, RESERVES, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, lobMarker, LOB, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, lobMarker, SEGMENTS, perilMarker, PERILS, pathWithoutChannel, channel, results)
        }
        for (String path : outputPathsByMarkerInterface.get(legalEntityMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentsByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, reserveMarker, RESERVES, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, contractMarker, CONTRACTS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, lobMarker, SEGMENTS, pathWithoutChannel, channel, results)
        }
        for (String path : outputPathsByMarkerInterface.get(structureMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentsByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, reserveMarker, RESERVES, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, contractMarker, CONTRACTS, pathWithoutChannel, channel, results)
            extendedPaths(componentsByMarkerInterface, lobMarker, SEGMENTS, pathWithoutChannel, channel, results)
        }
        if (LOG.isDebugEnabled()) {
            results.each { LOG.debug it }
        }
        return results
    }

    /**
     * @param componentsByMarkerInterface , key marker interface, values list of implementing components
     * @param markerClass
     * @param markerPath is inserted between pathWithoutChannel and channel
     * @param pathWithoutChannel
     * @param channel
     * @param results extended paths are added to this set
     */
    private static extendedPaths(Multimap<Class, Component> componentsByMarkerInterface, Class markerClass,
                                 String markerPath, String pathWithoutChannel, String channel, Set<String> results) {
        for (Component drillDownComponent : componentsByMarkerInterface.get(markerClass)) {
            extendedPath(drillDownComponent, new StringBuilder(pathWithoutChannel), markerPath, channel, results)
        }
    }

    private
    static void extendedPath(Component drillDownComponent, StringBuilder builder, String markerPath, String channel, Set<String> results) {
        if (drillDownComponent instanceof DynamicComposedComponent) return
        builder.append(PATH_SEPARATOR)
        builder.append(markerPath)
        builder.append(PATH_SEPARATOR)
        builder.append(drillDownComponent.name)
        builder.append(PATH_SEPARATOR)
        builder.append(channel)
        results.add(builder.toString())
    }

    private static extendedPaths(Multimap<Class, Component> componentsByMarkerInterface, Class markerClass1,
                                 String markerPath1, Class markerClass2, String markerPath2, String pathWithoutChannel,
                                 String channel, Set<String> results) {
        for (Component drillDownComponent : componentsByMarkerInterface.get(markerClass1)) {
            if (drillDownComponent instanceof DynamicComposedComponent) continue
            StringBuilder builder = new StringBuilder(pathWithoutChannel)
            builder.append(PATH_SEPARATOR)
            builder.append(markerPath1)
            builder.append(PATH_SEPARATOR)
            builder.append(drillDownComponent.name)
            for (Component drillDownComponentInner : componentsByMarkerInterface.get(markerClass2)) {
                extendedPath(drillDownComponentInner, new StringBuilder(builder.toString()), markerPath2, channel, results)
            }
        }
    }

    public static Set<String> pathsExtendedWithPeriod(List<String> paths, List<String> periodLabels) {
        Set<String> results = new HashSet<String>()
        for (String path : paths) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            for (String periodLabel : periodLabels) {
                StringBuilder builder = new StringBuilder(pathWithoutChannel)
                builder.append(PATH_SEPARATOR)
                builder.append(PERIOD)
                builder.append(PATH_SEPARATOR)
                builder.append(periodLabel)
                builder.append(PATH_SEPARATOR)
                builder.append(channel)
                results.add(builder.toString())
            }
        }
        return results
    }

    public static Set<String> pathsExtendedWithType(Collection<String> paths, Collection<String> typeLabels) {
        Set<String> results = new HashSet<String>()
        for (String path : paths) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            for (String type : typeLabels) {
                StringBuilder builder = new StringBuilder(pathWithoutChannel)
                builder.append(PATH_SEPARATOR)
                builder.append(SPLIT)
                builder.append(PATH_SEPARATOR)
                builder.append(type)
                builder.append(PATH_SEPARATOR)
                builder.append(channel)
                results.add(builder.toString())
            }
        }
        return results
    }

    private static String getPathBase(String path) {
        int separatorPositionBeforeChannel = path.lastIndexOf(":");
        return path.substring(0, separatorPositionBeforeChannel);
    }

    private static String getChannel(String path) {
        return path.split(PATH_SEPARATOR)[-1];
    }

    private static String getComponentName(String path) {
        return path.split(PATH_SEPARATOR)[-2];
    }

    private static Set<String> getAllPossibleOutputFields(Component component) {
        Set<String> results = []
        component.properties.each { String key, value ->
            if (key.startsWith("out")) {
                results.addAll(obtainFields(value))
            } else if (value instanceof Component) {
                results.addAll(getAllPossibleOutputFields(value))
            }
        }
        return results
    }

    private static Set<String> obtainFields(PacketList packetList) {
        Packet packet = packetList.getType().newInstance()
        return packet.getValuesToSave().keySet()
    }
}
