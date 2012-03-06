package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.components.IComponentMarker
import org.pillarone.riskanalytics.core.components.ComposedComponent


class ModelHelper {

    // todo(sku): try to reuse same constants of PC project
    private static final String PATH_SEPARATOR = ':'
    private static final String PERIOD = 'period'
    private static final String RESERVE_RISK = 'reserveRisk'
    private static final String PREMIUM_RISK = 'premiumRisk'
    private static final String PERILS = "claimsGenerators"
    private static final String RESERVES = "claimsGenerators"
    private static final String CONTRACTS = "reinsuranceContracts"
    private static final String LOB = "linesOfBusiness"
    private static final String SEGMENTS = "segments"
    private static final String SEGMENT_MARKER = "SegmentMarker"
    private static final String PERIL_MARKER = "PerilMarker"
    private static final String RESERVE_MARKER = "IReserveMarker"
    private static final String CONTRACT_MARKER = "IReinsuranceContractMarker"
    private static final String LEGAL_ENTITY_MARKER = "ILegalEntityMarker"


    /**
     * A static helper method which obtains all possible output paths of this model
     * @param model A model with all parameters injected
     * @return All possible fields
     */
    public static Set<String> getAllPossibleFields(Model model, boolean includeInceptionPeriodFields) {
        Set<String> results = []
        model.properties.each { String key, value ->
            if (value instanceof Component) {
                results.addAll(getAllPossibleOutputFields(value))
            }
        }
        if (includeInceptionPeriodFields) {
            results.add(RESERVE_RISK)
            results.add(PREMIUM_RISK)
        }
        return results
    }

    /**
     * A static helper method which obtains all possible output fields of this model
     * @param model A model with all parameters injected
     * @return All possible paths
     */
    public static Set<String> getAllPossibleOutputPaths(Model model, List<String> drillDownPaths) {
        String prefix = model.class.simpleName - "Model"
        Map<Class, List<String>> outputPathsByMarkerInterface = new HashMap<Class, List<String>>()
        Set<String> results = []
        model.properties.each { String key, value ->
            if (value instanceof Component) {
                results.addAll(getAllPossibleOutputPaths(prefix + ":${key}", value, outputPathsByMarkerInterface, drillDownPaths))
            }
        }
        Map<Class, List<String>> componentNameByMarkerInterface = new HashMap<Class, List<String>>()
        collectComponentNamesByMarkerInterface(model.allComponents, componentNameByMarkerInterface)
        results.addAll getPossibleDrillDownOutputPaths(outputPathsByMarkerInterface, componentNameByMarkerInterface)
        return injectStructure(results, ModelStructure.getStructureForModel(model.class))
    }

    private static Set<String> injectStructure(Set<String> results, ModelStructure structure) {
        structure.load()
        ConfigObject lines = structure.data.company
        if (lines.size() > 0) {
            Map repls = [:]
            lines.each {String line, data ->
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

    private static Set<String> getAllPossibleOutputPaths(String prefix, Component component,
                                                         Map<Class, List<String>> outputPathsByMarkerInterface,
                                                         List<String> drillDownPaths) {
        Set<String> results = []
        component.properties.each { String key, value ->
            if (key.startsWith("out")) {
                String path = prefix + ":${key}"
                results.add path
                if ((drillDownPaths != null) && drillDownPaths.contains(path)) {
                    findDrillDownCandidates component, path, outputPathsByMarkerInterface
                }
            } else if (value instanceof Component) {
                results.addAll(getAllPossibleOutputPaths(prefix + ":${key}", value, outputPathsByMarkerInterface, drillDownPaths))
            }
        }
        return results
    }

    private static void findDrillDownCandidates(Component component, String path,
                                                Map<Class, List<String>> outputPathsByMarkerInterface) {
        for (Class intf : component.class.interfaces) {
            if (IComponentMarker.isAssignableFrom(intf)) {
                List components = outputPathsByMarkerInterface.get(intf)
                if (components) {
                    components.add(path)
                }
                else {
                    outputPathsByMarkerInterface.put(intf, [path])
                }
            }
        }
    }

    /**
     * @param components
     * @param componentNameByMarkerInterface this map is filled by traversing all components including nested and checking
     *                                          for every component if it implements a marker interface
     */
    public static void collectComponentNamesByMarkerInterface(List<Component> components, Map<Class, List<String>> componentNameByMarkerInterface) {
        for (Component component : components) {
            for (Class intf : component.class.interfaces) {
                if (IComponentMarker.isAssignableFrom(intf)) {
                    List<String> componentNames = componentNameByMarkerInterface.get(intf)
                    if (componentNames == null) {
                        componentNames = new ArrayList<String>()
                        componentNameByMarkerInterface.put(intf, componentNames)
                    }
                    componentNames.add(component.name)
                }
            }
            if (component instanceof ComposedComponent) {
                collectComponentNamesByMarkerInterface component.allSubComponents(), componentNameByMarkerInterface
            }
        }
    }

    private static Set<String> getPossibleDrillDownOutputPaths(Map<Class, List<String>> outputPathsByMarkerInterface,
                                                               Map<Class, List<String>> componentNameByMarkerInterface) {
        Set<String> results = []
        Class lobMarker = componentNameByMarkerInterface.keySet().find { clazz -> clazz.name.contains(SEGMENT_MARKER) }
        Class perilMarker = componentNameByMarkerInterface.keySet().find { clazz -> clazz.name.contains(PERIL_MARKER) }
        Class reserveMarker = componentNameByMarkerInterface.keySet().find { clazz -> clazz.name.contains(RESERVE_MARKER)}
        Class contractMarker = componentNameByMarkerInterface.keySet().find { clazz -> clazz.name.contains(CONTRACT_MARKER) }
        Class legalEntityMarker = componentNameByMarkerInterface.keySet().find { clazz -> clazz.name.contains(LEGAL_ENTITY_MARKER) }

        for (String path : outputPathsByMarkerInterface.get(lobMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentNameByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, reserveMarker, RESERVES, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, contractMarker, CONTRACTS, pathWithoutChannel, channel, results)
        }
        for (String path : outputPathsByMarkerInterface.get(contractMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentNameByMarkerInterface, lobMarker, LOB, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, lobMarker, SEGMENTS, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, reserveMarker, RESERVES, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, lobMarker, LOB, contractMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, lobMarker, SEGMENTS, contractMarker, PERILS, pathWithoutChannel, channel, results)
        }
        for (String path : outputPathsByMarkerInterface.get(legalEntityMarker)) {
            String pathWithoutChannel = getPathBase(path)
            String channel = getChannel(path)
            extendedPaths(componentNameByMarkerInterface, perilMarker, PERILS, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, contractMarker, CONTRACTS, pathWithoutChannel, channel, results)
            extendedPaths(componentNameByMarkerInterface, lobMarker, SEGMENTS, pathWithoutChannel, channel, results)
        }
        return results
    }

    private static extendedPaths(Map<Class, List<String>> componentNameByMarkerInterface, Class markerClass,
                                       String markerPath, String pathWithoutChannel, String channel, Set<String> results) {
        for (String drillDownComponentName: componentNameByMarkerInterface.get(markerClass)) {
            StringBuilder builder = new StringBuilder(pathWithoutChannel)
            builder.append(PATH_SEPARATOR)
            builder.append(markerPath)
            builder.append(PATH_SEPARATOR)
            builder.append(drillDownComponentName)
            builder.append(PATH_SEPARATOR)
            builder.append(channel)
            results.add(builder.toString())
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

    private static extendedPaths(Map<Class, List<String>> componentNameByMarkerInterface, Class markerClass1,
                                       String markerPath1, Class markerClass2, String markerPath2, String pathWithoutChannel,
                                       String channel, Set<String> results) {
        for (String drillDownComponentName1: componentNameByMarkerInterface.get(markerClass1)) {
            StringBuilder builder = new StringBuilder(pathWithoutChannel)
            builder.append(PATH_SEPARATOR)
            builder.append(markerPath1)
            builder.append(PATH_SEPARATOR)
            builder.append(drillDownComponentName1)
            builder.append(PATH_SEPARATOR)
            for (String drillDownComponentName2 : componentNameByMarkerInterface.get(markerClass2)) {
                StringBuilder builder2 = new StringBuilder(builder.toString());
                builder2.append(markerPath2)
                builder2.append(PATH_SEPARATOR)
                builder2.append(drillDownComponentName2)
                builder2.append(PATH_SEPARATOR)
                builder2.append(channel)
                results.add(builder2.toString())
            }
        }
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
