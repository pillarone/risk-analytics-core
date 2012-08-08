package org.pillarone.riskanalytics.core.wiring

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.util.GroovyUtils
import groovyx.gpars.agent.Agent

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
        return GroovyUtils.getProperties(sender).find {Map.Entry entry -> entry.value.is(source)}.key
    }

    static void forAllComponents(Component target, Closure whatToDo) {
        whatToDo("", target)
        forAllSubComponents(target, whatToDo)
    }

    static void forAllComponents(def target, Closure whatToDo) {
        forAllSubComponents(target, whatToDo)
    }

    static void forAllSubComponents(def target, Closure whatToDo) {
        GroovyUtils.getProperties(target).each { Map.Entry entry ->
            def propertyName = entry.key
            def propertyValue = entry.value
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


    static Agent guard = new Agent()

    static void use(Class category, Closure work) {
        if (LOG.isDebugEnabled()) LOG.debug "starting wiring for ${work.delegate.getName()} with ${category.name}."
        def changedClasses = [] as Set
        boolean componentFound = GroovyUtils.getProperties(work.delegate).any { Map.Entry entry -> entry.value instanceof Component }
        assert componentFound, "Components to be wired must be properties of the callee!"

        guard.send {
            forAllComponents(work.delegate) { componentName, component ->
                changedClasses << component.getClass()
                ExpandoMetaClass emc = GrailsClassUtils.getExpandoMetaClass(component.getClass())
                emc.getProperty = { name -> category.doGetProperty(delegate, name) }
                emc.setProperty = { name, value -> category.doSetProperty(delegate, name, value) }
            }
        }
        guard.val

        try {
            work()
        } finally {
            guard.send {
                for (changedClass in changedClasses) {
                    ExpandoMetaClass emc = GrailsClassUtils.getExpandoMetaClass(changedClass)
                    emc.getProperty = defaultGetter
                    emc.setProperty = defaultSetter
                }
            }
            guard.val
            if (LOG.isDebugEnabled()) LOG.debug "restoring MCs done."
        }
    }
}