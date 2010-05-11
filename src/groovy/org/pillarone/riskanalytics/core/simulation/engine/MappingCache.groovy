package org.pillarone.riskanalytics.core.simulation.engine;

import org.pillarone.riskanalytics.core.output.CollectorMapping;
import org.pillarone.riskanalytics.core.output.FieldMapping;


import org.pillarone.riskanalytics.core.output.PathMapping
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model;

/**
 * A cache which enables fast access to PathMapping, FieldMapping & CollectorMapping objects.
 * It can be used by ICollectingModeStrategy classes.
 *
 * During initialization all existing field & collector mappings and all path mappings belonging to this model
 * are pre-loaded. This is more than necessary, but a lot faster than loading single objects.
 */
public class MappingCache {

    private Map<String, PathMapping> paths;
    private Map<String, FieldMapping> fields;
    private Map<String, CollectorMapping> collectors;

    private static Log LOG = LogFactory.getLog(MappingCache)

    /**
     * Creates an empty cache. Can be initialized with initCache().
     */
    public MappingCache() {
        paths = new HashMap<String, PathMapping>();
        fields = new HashMap<String, FieldMapping>();
        collectors = new HashMap<String, CollectorMapping>();
    }

    /**
     * Creates and fills a cache with the required mappings for a model.
     */
    public MappingCache(Model model) {
        this()
        initCache(model)
    }

    /**
     * Clears the existing cache and fills it with the required mappings for a model.
     */
    public void initCache(Model model) {
        paths.clear()
        fields.clear()
        collectors.clear()

        addPaths(PathMapping.executeQuery("from PathMapping where pathName like '${model.getClass().simpleName - "Model"}%'"))
        addCollectors(CollectorMapping.list())
        addFields(FieldMapping.list())
    }

    protected void addCollectors(List<CollectorMapping> collectorMappings) {
        for (CollectorMapping collectorMapping: collectorMappings) {
            collectors.put(collectorMapping.collectorName, collectorMapping);
        }
        LOG.debug("loaded ${collectors.size()} collector mappings")
    }

    protected void addFields(List<FieldMapping> fieldMappings) {
        for (FieldMapping fieldMapping: fieldMappings) {
            fields.put(fieldMapping.fieldName, fieldMapping);
        }
        LOG.debug("loaded ${fields.size()} field mappings")
    }

    protected void addPaths(List<PathMapping> pathMappings) {
        for (PathMapping pathMapping: pathMappings) {
            paths.put(pathMapping.pathName, pathMapping);
        }
        LOG.debug("loaded ${paths.size()} path mappings")
    }

    /**
     * Return the PathMapping domain object of the given path.
     * If the path does not exist yet, it gets persisted and added to the cache.
     */
    public PathMapping lookupPath(String path) {
        PathMapping pathMapping = paths.get(path)
        if (pathMapping == null) {
            pathMapping = new PathMapping(pathName: path).save()
            paths.put(path, pathMapping)
        }
        return pathMapping;
    }

    /**
     * Return the CollectorMapping domain object of the given collector.
     * If the collector does not exist yet, it gets persisted and added to the cache.
     */
    public CollectorMapping lookupCollector(String collector) {
        CollectorMapping collectorMapping = collectors.get(collector)
        if (collectorMapping == null) {
            collectorMapping = new CollectorMapping(collectorName: collector).save()
            collectors.put(collector, collectorMapping)
        }
        return collectorMapping;
    }

    /**
     * Return the FieldMapping domain object of the given field.
     * If the field does not exist yet, it gets persisted and added to the cache.
     */
    public FieldMapping lookupField(String field) {
        FieldMapping fieldMapping = fields.get(field)
        if (fieldMapping == null) {
            fieldMapping = new FieldMapping(fieldName: field).save()
            fields.put(field, fieldMapping)
        }
        return fieldMapping;
    }
}
