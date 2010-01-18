package org.pillarone.riskanalytics.core.packets;

import java.util.ArrayList;
import java.util.Collection;


public class PacketList<E extends Packet> extends ArrayList<E> {

    private Class elementType;

    public PacketList() {
        this(Packet.class);
    }

    public PacketList(Collection<? extends E> c) {
        super(c);
        elementType = Packet.class;
    }

    public PacketList(Class<? extends Packet> typeOfElements) {
        super();
        if (!Packet.class.isAssignableFrom(typeOfElements)) {
            throw new IllegalArgumentException("type of Elements must be of type Packet.");
        }
        elementType = typeOfElements;
    }

    public boolean add(E o) {
        isCompatibleElement(o);
        return super.add(o);
    }

    public void add(int index, E element) {
        isCompatibleElement(element);
        super.add(index, element);
    }

    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            isCompatibleElement(e);
        }
        return super.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        for (E e : c) {
            isCompatibleElement(e);
        }
        return super.addAll(index, c);
    }

    public boolean isCompatibleTo(PacketList otherList) {
        return elementType.isAssignableFrom(otherList.elementType);
    }

    private void isCompatibleElement(E o) {
        if (!elementType.isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException("Adding an element of type " + o.getClass().getName() + " is not allowed for a list with type " + elementType.getName());
        }
    }

    public Class getType() {
        return elementType;
    }

}