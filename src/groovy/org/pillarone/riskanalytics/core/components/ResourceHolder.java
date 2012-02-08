package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber;


public class ResourceHolder<E extends IResource> {

    private E resource;
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
}
