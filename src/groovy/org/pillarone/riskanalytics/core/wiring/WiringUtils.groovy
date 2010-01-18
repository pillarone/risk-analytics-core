package org.pillarone.riskanalytics.core.wiring

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList

public class WiringUtils {

    static final Logger LOG = Logger.getLogger(WiringUtils)

    /**
     * Remove output replication transmitters to none wired out channels
     */
    public static void optimizeWiring(ComposedComponent composedComponent) {
        LOG.debug "Trying to optimize wiring"
        List wiredOutChannels = composedComponent.allOutputTransmitter*.source
        Collection replicatorToBeRemoved = composedComponent.allOutputReplicationTransmitter.findAll {transmitter ->
            !wiredOutChannels.any { it.is transmitter.target }
        }
        replicatorToBeRemoved.each {Transmitter t ->
            LOG.debug "removing $t"
            composedComponent.allOutputReplicationTransmitter.remove(t)
            t.sender.allOutputTransmitter.remove(t)
        }
    }

    public static String getSenderChannelName(Component sender, PacketList source) {
        return sender.properties.find {k, v -> v.is(source)}.key
    }

    static void forAllComponents(Component target, Closure whatToDo) {
        whatToDo("", target)
        forAllSubComponents(target, whatToDo)
    }

    static void forAllComponents(def target, Closure whatToDo) {
        forAllSubComponents(target, whatToDo)
    }

    static void forAllSubComponents(def target, Closure whatToDo) {
        target.properties.each {propertyName, propertyValue ->
            if (propertyValue != null && propertyValue instanceof Component) {
                whatToDo(propertyName, propertyValue)
                forAllSubComponents(target[propertyName], whatToDo)
            }
        }
    }

    static defaultGetter = { propName ->
        try {
            return delegate."${GrailsClassUtils.getGetterName(propName)}"()
        } catch (MissingMethodException e) {
            if (LOG.isDebugEnabled()) LOG.debug "resolving $propName via propertyMissing"
            return delegate.propertyMissing(propName)
        }
    }
    static defaultSetter = { name, value ->
        delegate."${GrailsClassUtils.getSetterName(name)}"(value)
    }

    static void use(Class category, Closure work) {
        if (LOG.isDebugEnabled()) LOG.debug "starting wiring for ${work.delegate.getName()} with ${category.name}."
        def changedClasses = [] as Set
        boolean componentFound = work.delegate.properties.any { k, v -> v instanceof Component }
        assert componentFound, "Components to be wired must be properties of the callee!"
        forAllComponents(work.delegate) { componentName, component ->
            changedClasses << component.getClass()
            ExpandoMetaClass emc = GrailsClassUtils.getExpandoMetaClass(component.getClass())
            emc.getProperty = { name -> category.doGetProperty(delegate, name) }
            emc.setProperty = { name, value -> category.doSetProperty(delegate, name, value) }
        }

        try {
            work()
        } finally {
            for (changedClass in changedClasses) {
                ExpandoMetaClass emc = GrailsClassUtils.getExpandoMetaClass(changedClass)
                emc.getProperty = defaultGetter
                emc.setProperty = defaultSetter
            }
            if (LOG.isDebugEnabled()) LOG.debug "restoring MCs done."
        }
    }
}