package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber;

import java.io.Serializable;


public class ResourceHolder<E extends IResource> implements Cloneable, Serializable {

    private transient E resource;
    private String name;
    private VersionNumber version;
    private Class resourceClass;

    public ResourceHolder(E resource, String name, VersionNumber versionNumber, Class resourceClass) {
        this.resource = resource;
        this.name = name;
        this.version = versionNumber;
        this.resourceClass = resourceClass;
    }

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
        return resource;
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
        clone.resource = null;
        clone.version = new VersionNumber(this.version.toString());
        return clone;
    }

    @Override
    public String toString() {
        return name + " v" + (version != null ? version.toString() : null);
    }
}
