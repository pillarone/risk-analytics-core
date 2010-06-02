package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.Packet


class ModelHelper {

    /**
     * A static helper method which obtains all possible output paths of this model
     * @param model A model with all parameters injected
     * @return All possible paths
     */
    public static Set<String> getAllPossibleFields(Model model) {
        Set<String> results = []
        model.properties.each { String key, value ->
            if (value instanceof Component) {
                results.addAll(getAllPossibleOutputFields(value))
            }
        }
        return results
    }

    /**
     * A static helper method which obtains all possible output fields of this model
     * @param model A model with all parameters injected
     * @return All possible fields
     */
    public static Set<String> getAllPossibleOutputPaths(Model model) {
        String prefix = model.class.simpleName - "Model"

        Set<String> results = []
        model.properties.each { String key, value ->
            if (value instanceof Component) {
                results.addAll(getAllPossibleOutputPaths(prefix + ":${key}", value))
            }
        }
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

    private static Set<String> getAllPossibleOutputPaths(String prefix, Component component) {
        Set<String> results = []
        component.properties.each { String key, value ->
            if (key.startsWith("out")) {
                results.add(prefix + ":${key}")
            } else if (value instanceof Component) {
                results.addAll(getAllPossibleOutputPaths(prefix + ":${key}", value))
            }
        }
        return results
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
