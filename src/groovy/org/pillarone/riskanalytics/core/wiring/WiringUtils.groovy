package org.pillarone.riskanalytics.core.wiring

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.model.Model

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
        //if (LOG.isInfoEnabled()) LOG.info "starting wiring for ${work.delegate.getName()} with ${category.name}."
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
            if (LOG.isDebugEnabled()) logWiring (work.delegate)
        } finally {
            for (changedClass in changedClasses) {
                ExpandoMetaClass emc = GrailsClassUtils.getExpandoMetaClass(changedClass)
                emc.getProperty = defaultGetter
                emc.setProperty = defaultSetter
            }
            if (LOG.isDebugEnabled()) LOG.debug "restoring MCs done."
        }
    }

    private static void logWiring(def target) {
        String logMsg = "#Thread:" + Thread.currentThread().getId() + "\n";
        if (target instanceof Model) {
            Model model = (Model) target;
            for (Component c: model.allComponents) {
                logMsg += logTransmitter(c)
            }
        } else {
            logMsg += logTransmitter(target);
        }
        LOG.info(logMsg);
    }

    private static String logTransmitter(Component c) {

        String logMsg = "### Component ${c.getName()} (${c.getClass().getCanonicalName()}) \nINPUT TRANSMITTER:\n"
        for (Transmitter t: c.allInputTransmitter) {
            logMsg += "\t" + t + "\n"
        }
        logMsg += "OUTPUT TRANSMITTER:\n";

        for (Transmitter t: c.allOutputTransmitter) {
            logMsg += "\t" + t + "\n"
        }

        return logMsg;
    }

}