package org.pillarone.riskanalytics.core.components;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber;

import java.io.Serializable;


public class ResourceHolder<E extends IResource> implements Cloneable, Serializable {

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
    public boolean equals(Object obj) {
        if (obj instanceof ResourceHolder) {
            ResourceHolder holder = (ResourceHolder) obj;
            return new EqualsBuilder().append(name, holder.name).
                    append(version.toString(), holder.version.toString()).
                    append(resourceClass.getName(), holder.resourceClass.getName()).isEquals();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(version.toString()).
                append(resourceClass.getName()).toHashCode();
    }
}
