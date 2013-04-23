package org.pillarone.riskanalytics.core

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.IComponentMarker
import org.pillarone.riskanalytics.core.components.IResource
import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
@CompileStatic
class MarkerInterfaceCollector implements IModelVisitor {

    private Set<Class> interfaces = []
    private Map<Component, Set<Class>> interfacesPerComponent = [:]
    private Multimap<Class, Component> componentsPerMarkerInterface = ArrayListMultimap.create()

    Set<Class> getMarkerInterfaces() {
        interfaces
    }

    Set<Class> getMarkerInterfaces(Component component) {
        interfacesPerComponent.get(component)
    }

    Multimap<Class, Component> componentsByMarkerInterface() {
        componentsPerMarkerInterface
    }

    void visitModel(Model model) {
    }

    void visitComponent(Component component, ModelPath path) {
        Set<Class> componentMarkerIntf = []
        Set<Class> componentIntf = []
        getInterfaces(component.class, componentIntf)
        for (Class intf : componentIntf) {
            if (IComponentMarker.isAssignableFrom(intf)) {
                componentMarkerIntf << intf
                interfaces << intf
            }
        }
        if (!componentMarkerIntf.isEmpty()) {
            interfacesPerComponent.put(component, componentMarkerIntf)
            for (Class intf : componentMarkerIntf) {
                componentsPerMarkerInterface.put(intf, component)
            }
        }
    }

    void visitResource(IResource resource, ModelPath path) {
    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
    }

    private static void getInterfaces(Class clazz, Set<Class> interfaces) {
        interfaces.addAll clazz.interfaces
        if (clazz.superclass != Component.class) {
            getInterfaces(clazz.superclass, interfaces)
        }
    }
}
