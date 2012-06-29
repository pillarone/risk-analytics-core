package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.simulation.item.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ResourceRegistry {

    private static ThreadLocal<Map<ResourceHolder, IResource>> resources = new ThreadLocal<Map<ResourceHolder, IResource>>() {
        @Override
        protected Map<ResourceHolder, IResource> initialValue() {
            return new HashMap<ResourceHolder, IResource>();
        }
    };

    public static void preLoad(List<Resource> resourceList) {
        Map<ResourceHolder, IResource> map = resources.get();
        for (Resource resource : resourceList) {
            map.put(new ResourceHolder(resource.getModelClass(), resource.getName(), resource.getVersionNumber()), resource.createResourceInstance());
        }
    }

    public static IResource getResourceInstance(ResourceHolder holder) {
        Map<ResourceHolder, IResource> map = resources.get();

        IResource resourceInstance = map.get(holder);
        if (resourceInstance == null) {
            Resource resource = new Resource(holder.getName(), holder.getResourceClass());
            resource.setVersionNumber(holder.getVersion());
            resourceInstance = resource.createResourceInstance();
            map.put(holder, resourceInstance);
        }

        return resourceInstance;
    }

    public static void clear() {
        resources.get().clear();
    }

}
