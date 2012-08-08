package org.pillarone.riskanalytics.core.wiring

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.util.GroovyUtils

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

    static void use(Class category, Closure work) {
        if (LOG.isDebugEnabled()) LOG.debug "starting wiring for ${work.delegate.getName()} with ${category.name}."
        boolean componentFound = GroovyUtils.getProperties(work.delegate).any { Map.Entry entry -> entry.value instanceof Component }
        assert componentFound, "Components to be wired must be properties of the callee!"

        def changedComponentsAndTheirMCs = []
        forAllComponents(work.delegate) { componentName, component ->
            if (component in changedComponentsAndTheirMCs.collect{ it.comp }) return
            changedComponentsAndTheirMCs << [comp:component, mc:component.metaClass]
            component.metaClass.mixin AccessorOverrideMixin
            component.mixinDelegate = component
            component.mixinCategory = category
        }
        try {
            work()
        } finally {
            for (componentAndMC in changedComponentsAndTheirMCs) {
                componentAndMC.comp.metaClass = componentAndMC.mc
            }
            if (LOG.isDebugEnabled()) LOG.debug "restoring MCs done."
        }
    }
}

class AccessorOverrideMixin {
    def mixinDelegate
    def mixinCategory
    def getProperty (name) {
        if (name == "mixinDelegate") { return mixinDelegate }
        if (name == "mixinCategory") { return mixinCategory }
        mixinCategory.doGetProperty(mixinDelegate, name)
    }
    void setProperty (name, value) {
        if (name == "mixinDelegate") { mixinDelegate = value; return }
        if (name == "mixinCategory") { mixinCategory = value; return }
        mixinCategory.doSetProperty(mixinDelegate, name, value)
    }
}