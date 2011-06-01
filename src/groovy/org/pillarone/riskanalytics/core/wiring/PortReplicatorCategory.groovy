package org.pillarone.riskanalytics.core.wiring

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.LinkedProperty
import org.pillarone.riskanalytics.core.wiring.SilentTransmitter
import org.pillarone.riskanalytics.core.wiring.Transmitter
import org.pillarone.riskanalytics.core.util.GroovyUtils

class PortReplicatorCategory {
    static final Logger LOG = Logger.getLogger(PortReplicatorCategory)

    private static ThreadLocal<IPacketListener> packetListener = new ThreadLocal<IPacketListener>() {

        @Override
        protected IPacketListener initialValue() {
            return null;
        }

    };

    public static void setPacketListener(IPacketListener packetListener) {
        this.packetListener.set(packetListener);
    }

    static void doSetProperty(Component receiver, String targetPropertyName, Object sender) {
        // guarded clause to check that only input - input channels are wired
        if (!targetPropertyName.startsWith("in") && !targetPropertyName.startsWith("out")) {
            receiver."${GrailsClassUtils.getSetterName(targetPropertyName)}"(sender)
            return
        }
        assert sender in LinkedProperty, "${PortReplicatorCategory.class.simpleName}:Only objects of class LinkedProperty can be wired but was: " + sender.dump()

        def source = ((LinkedProperty) sender).source
        def sourcePropertyName = ((LinkedProperty) sender).name

        PacketList sourceProperty = GroovyUtils.getProperties(source).get(sourcePropertyName)
        PacketList targetProperty = GroovyUtils.getProperties(receiver).get(targetPropertyName)

        if (!sourceProperty.isCompatibleTo(targetProperty)) {
            throw new IllegalArgumentException("Wiring only allowed with same types for input and output")
        }

        replicateChannels(sourceProperty, targetProperty, sourcePropertyName, receiver, source, targetPropertyName)
    }

    protected static void replicateChannels(PacketList sourceProperty, PacketList targetProperty, String sourcePropertyName, Component receiver, Component source, String targetPropertyName) {
        replicateInChannel(targetPropertyName, source, receiver, sourcePropertyName, targetProperty, sourceProperty)
        replicateOutChannel(targetProperty, receiver, sourceProperty, source, sourcePropertyName, targetPropertyName)
    }

    protected static void replicateInChannel(String targetPropertyName, Component source, Component receiver, String sourcePropertyName, PacketList targetProperty, PacketList sourceProperty) {
        // subClaimsGenerator.inUnderwritingInfo = this.inUnderwritingInfo
        // receiver                              = source
        if (sourcePropertyName.startsWith("in")) {
            if (!targetPropertyName.startsWith("in")) {
                throw new UnsupportedOperationException("Only matching ports can be replicated. [in = in | out = out]")
            }
            if (!isSubcomponent(source, receiver)) {
                throw new UnsupportedOperationException("Only port of subcomponents can be replicated")
            }
            ITransmitter transmitter = new Transmitter(source, sourceProperty, receiver, targetProperty)
            if (packetListener.get() != null) {
                transmitter = new TraceableTransmitter(transmitter, packetListener.get());
            }
            receiver.allInputTransmitter << transmitter
            source.allInputReplicationTransmitter << transmitter
        }
    }

    protected static void replicateOutChannel(PacketList targetProperty, Component receiver, PacketList sourceProperty, Component source, String sourcePropertyName, String targetPropertyName) {
        // this.outCoveredClaims = subClaimsGenerator.outCoveredClaims
        // receiver       = source
        if (sourcePropertyName.startsWith("out")) {
            if (!targetPropertyName.startsWith("out")) {
                throw new UnsupportedOperationException("Only matching ports can be replicated. [in = in | out = out]")
            }
            if (!isSubcomponent(receiver, source)) {
                throw new UnsupportedOperationException("Only port of subcomponents can be replicated")
            }
            ITransmitter transmitter = new SilentTransmitter(source, sourceProperty, receiver, targetProperty)
            if (packetListener.get() != null) {
                transmitter = new TraceableTransmitter(transmitter, packetListener.get());
            }
            source.allOutputTransmitter << transmitter
            receiver.allOutputReplicationTransmitter << transmitter
        }
    }

    static boolean isSubcomponent(Component compound, Component component) {
        for (p in compound.properties.values()) {
            if (p && (p.is(component) || (Collection.isAssignableFrom(p.class) && p.contains(component)))) {
                return true
            }
        }
        return false
    }

    static doGetProperty(Component self, String name) {
        LOG.debug "PortReplicatorCategory.doGetProperty(${self.getName()}, ${name})"
        if (name.startsWith("out") || name.startsWith("in")) {
            return new LinkedProperty(source: self, name: name)
        }
        try { return self."${GrailsClassUtils.getGetterName(name)}"() }
        catch (e) {
            LOG.debug "resolving $name via propertyMissing"
            return self.propertyMissing(name)
        }
    }
}
