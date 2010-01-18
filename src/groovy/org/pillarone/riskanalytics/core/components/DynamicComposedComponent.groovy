package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory as PRC

/*
do not remove the following imports:
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent

even though they are not required groovy will throw a TypeNotPresentException during unit test compilation
if they are not here...
 */
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.wiring.WiringUtils

abstract class DynamicComposedComponent extends ComposedComponent {

    private final List<Component> componentList = []

    public List<Component> getComponentList() {
        return componentList
    }

    public Component getComponentByName(String name) {
        for (Component c in componentList) {
            if (c.name == name) {
                return c
            }
        }
        return null
    }

    public void addSubComponent(Component component) {
        if (component == null || component.name == null) {
            throw new IllegalArgumentException("Sub component must not be null and must have a name set")
        }
        if (isComponentNameUnique(component)) {
            WiringUtils.forAllSubComponents(component) {propertyName, Component subComponent ->
                if (subComponent.name == null) {subComponent.name = propertyName}
            }
            componentList << component
        }
        else {
            throw new IllegalArgumentException("A component with the name ${component.name} already exists in this dynamic composed component")
        }
    }

    private boolean isComponentNameUnique(Component newComponent) {
        for (Component component: componentList) {
            if (component.name == newComponent.name) {
                return false
            }
        }
        return true
    }

    Object propertyMissing(String name) {
        Component component = componentList.find { it.name == name }
        if (component) {
            return component
        }
        throw new MissingPropertyException("Property $name not found.")
    }

    void propertyMissing(String name, Object args) {
        Component component = componentList.find { it.name == name }
        if (component) {
            int index = componentList.indexOf(component)
            componentList.set(index, args)
            return
        }
        throw new MissingPropertyException("Property $name not found.")
    }

    public Map getProperties() {
        Map p = super.getProperties()
        componentList.eachWithIndex {component, index ->
            p[component.name] = component
        }

        return p
    }

    boolean isDynamicSubComponent(Component component) {
        componentList.contains(component)
    }

    /**
     * Return a meaningful name for the group of all subcomponents of this DynamicComposedComponent
     * Used for display in SimulationTemplate
     */
    public String getGenericSubComponentName() {
        return "subcomponents"
    }

    abstract Component createDefaultSubComponent()

    void removeSubComponent(Component subComponent) {
        componentList.remove(subComponent)
    }

    int subComponentCount() {
        componentList.size()
    }

    /**
     *  Sub components are either properties on the component or in case
     *  of dynamically composed components stored in its componentList.
     * @return all sub components
     */
    public List<Component> allSubComponents() {
        List<Component> subComponents = super.allSubComponents();
        subComponents.addAll(componentList)
        return subComponents;
    }

    public void clear() {
        componentList.clear()
    }

    protected void replicateOutChannels(Component dynamicComponent, String channelName) {
        for (Component component: componentList) {
            doWire PRC, dynamicComponent, channelName, component, channelName
        }
    }

    protected void replicateInChannels(Component dynamicComponent, String channelName) {
        // PMO-650: sorting is necessary in order to produce reproducible results.
        Collections.sort(componentList, ComponentComparator.getInstance())
        for (Component component: componentList) {
            doWire PRC, component, channelName, dynamicComponent, channelName
        }
    }

    /**
     * Helper method for wiring when sender or receiver are determined dynamically
     */
    public static void doWire(category, receiver, inChannelName, sender, outChannelName) {
        category.doSetProperty(receiver, inChannelName, category.doGetProperty(sender, outChannelName))
    }
}
