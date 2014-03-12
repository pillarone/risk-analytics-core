package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber;

import java.io.Serializable;


public class Resourc$eHolder<E extends IResource> implements Cloneable, Serializable {

    private static final long serialVersionUID = 89572958934031L;

    private String name;
    private VersionNumber version;
    private Class resourceClass;

    public ResourceHolder(Class resourceClass) {
        this.resourceClass = resourceClass;
    }

    public ResourceHolder(Class resourceClass, String name, VersionNumber version) {
        this.name = name;
        this.resourceClass = resourceClass;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public E getResource() {
        return (E) ResourceRegistry.getResourceInstance(this);
    }

    public Class getResourceClass() {
        return resourceClass;
    }

    public VersionNumber getVersion() {
        return version;
    }

    @Override
    public ResourceHolder clone() throws CloneNotSupportedException {
        ResourceHolder clone = (ResourceHolder) super.clone();
        clone.version = new VersionNumber(this.version.toString());
        return clone;
    }

    @Override
    public String toString() {
        return name + " v" + (version != null ? version.toString() : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceHolder)) return false;

        ResourceHolder that = (ResourceHolder) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!resourceClass.equals(that.resourceClass)) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + resourceClass.hashCode();
        return result;
    }
}
