package org.pillarone.riskanalytics.core.packets;

import org.joda.time.DateTime;
import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.IComponentMarker;

import java.util.*;

public class Packet implements Cloneable, Comparable {
    public Component origin;

    // to associate the packet with its sender (as the origin gets modified by components only in some cases)
    public Component sender;
    public String senderChannelName;

    private Map<Class<? extends IComponentMarker>, IComponentMarker> markers = new HashMap<Class<? extends IComponentMarker>, IComponentMarker>();

    public UUID id;

    /**
     *  Setting the period property allows to persist a packet in a different period than the period it is created.
     *  Business components have to set the property. It is evaluated in AbstractCollectingModeStrategy.
     */
    public Integer period;

    private DateTime date;

    public Packet() {
    }

    public Packet(Packet original) {
        this.setOrigin(original.origin);
        this.setSender(original.sender);
        this.setSenderChannelName(original.senderChannelName);
        this.id=id;
    }

    @Override
    public Packet clone() {
        try {
            return (Packet) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The default implementation is to return a clone of the param toCopy
     *
     * @return a clone of toCopy
     */
    public Packet copy() {
        return (Packet) clone();
    }

    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        return new HashMap<String, Number>();
    }

    /**
     * Compare Packets using the System.identityHashCode() function
     */
    public int compareTo(Object o) {
        return (System.identityHashCode(this) - System.identityHashCode(o));
    }

    public Component getOrigin() {
        return origin;
    }

    public void setOrigin(Component origin) {
        this.origin = origin;
    }

    public Component getSender() {
        return sender;
    }

    public void setSender(Component sender) {
        this.sender = sender;
    }

    public String getSenderChannelName() {
        return senderChannelName;
    }

    public void setSenderChannelName(String senderChannelName) {
        this.senderChannelName = senderChannelName;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public void addMarker(Class<? extends IComponentMarker> marker, IComponentMarker sender) {
        markers.put(marker, sender);
    }

    public IComponentMarker getMarkedSender(Class<? extends IComponentMarker> marker) {
        return markers.get(marker);
    }

    public void initDefaultPacket(){
    }
}
