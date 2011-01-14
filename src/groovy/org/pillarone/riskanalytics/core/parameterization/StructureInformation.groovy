package org.pillarone.riskanalytics.core.parameterization

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.util.GroovyUtils

class StructureInformation implements IStructureInformation {
    Map componentsOfLine = [:]
    BiMap componentPaths = HashBiMap.create()

    public StructureInformation(ConfigObject structure, Model model) {
        ConfigObject extendedStructure = extendWithModelProperties(structure, model)

        String path = model.class.simpleName - 'Model'
        extendedStructure.company.each {line, value ->
            try {
                def lobObject = model.getProperty(line)
                componentPaths[lobObject] = "$path:$line"
            } catch (MissingPropertyException e) {

            }
            extendedStructure.company[line].components.values().each {
                String componentPath = path + ":$line" + ":${it.name}"
                componentPaths[it] = componentPath
                componentsOfLine[it] = line
                resolveSubComponents(it, componentPath).each {
                    componentsOfLine[it] = line
                }
            }
        }
    }

    protected ConfigObject extendWithModelProperties(ConfigObject structure, Model model) {
        ConfigObject extendedStructure = structure.merge(new ConfigObject())
        ConfigObject flatStructure = structure.flatten()
        if (model) {
            model.properties.each {name, value ->
                if (value instanceof Component) {
                    if (!flatStructure.containsValue(value)) {
                        componentsOfLine[value] = name
                        ConfigObject line = extendedStructure.company[name]
                        ConfigObject components = line.components
                        value.properties.each {subComponentName, subComponent ->
                            if (subComponent instanceof Component) {
                                components[subComponentName] = subComponent
                            }
                        }
                    }
                }
            }
        }
        return extendedStructure
    }

    private List resolveSubComponents(Component c, String path) {
        List result = []
        GroovyUtils.getProperties(c).each {Map.Entry entry ->
            def v = entry.value
            if (v instanceof Component) {
                componentPaths[v] = path + ":${v.name}"
                result.addAll(resolveSubComponents(v, path + ":${v.name}"))
                result << v
            }
        }
        return result
    }

    public String getLine(Packet packet, String property) {
        componentsOfLine[packet.getProperties().get(property)]
    }

    public String getLine(Packet packet) {
        componentsOfLine[packet.getOrigin()]
    }

    public String getLine(Component component) {
        componentsOfLine[component]
    }

    /**
     * @return path with sender channel name
     */
    public String getPath(Packet packet) {
        internalGetPath(packet)
    }

    /**
     * @return path without sender channel name
     */
    public String getComponentPath(Packet packet) {
        componentPaths[packet.sender] != null ? componentPaths[packet.sender] : ""
    }

    private def internalGetPath(Packet packet) {
        componentPaths[packet.sender] != null ? componentPaths[packet.sender] + ":${packet.senderChannelName}" : "${packet.senderChannelName}"
    }
}
