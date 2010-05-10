package org.pillarone.riskanalytics.core.simulation.engine;

import org.pillarone.riskanalytics.core.output.CollectorMapping;
import org.pillarone.riskanalytics.core.output.FieldMapping;


import org.pillarone.riskanalytics.core.output.PathMapping
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model;

/**
 * A cache which enables fast access to PathMapping, FieldMapping & CollectorMapping ids.
 * It can be used by ICollectingModeStrategy classes.
 *
 * During initialization all existing field & collector mappings and all path mappings belonging to this model
 * are pre-loaded. This is more than necessary, but a lot faster than loading single objects.
 *
 * For performance reasons (hibernate overhead), only the ids are stored and not the objects itself.,
 */
public class MappingCache {

    private Map<String, Long> paths;
    private Map<String, Long> fields;
    private Map<String, Long> collectors;

    private static Log LOG = LogFactory.getLog(MappingCache)

    /**
     * Creates an empty cache. Can be initialized with initCache().
     */
    public MappingCache() {
        paths = new HashMap<String, Long>();
        fields = new HashMap<String, Long>();
        collectors = new HashMap<String, Long>();
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
            collectors.put(collectorMapping.collectorName, collectorMapping.id);
        }
        LOG.debug("loaded ${collectors.size()} collector mappings")
    }

    protected void addFields(List<FieldMapping> fieldMappings) {
        for (FieldMapping fieldMapping: fieldMappings) {
            fields.put(fieldMapping.fieldName, fieldMapping.id);
        }
        LOG.debug("loaded ${fields.size()} field mappings")
    }

    protected void addPaths(List<PathMapping> pathMappings) {
        for (PathMapping pathMapping: pathMappings) {
            paths.put(pathMapping.pathName, pathMapping.id);
        }
        LOG.debug("loaded ${paths.size()} path mappings")
    }

    /**
     * Return the PathMapping domain object of the given path.
     * If the path does not exist yet, it gets persisted and added to the cache.
     */
    public Long lookupPath(String path) {
        Long pathMappingId = paths.get(path)
        if (pathMappingId == null) {
            PathMapping pathMapping = new PathMapping(pathName: path).save()
            paths.put(path, pathMapping.id)
            pathMappingId = pathMapping.id
        }
        return pathMappingId;
    }

    /**
     * Return the CollectorMapping domain object of the given collector.
     * If the collector does not exist yet, it gets persisted and added to the cache.
     */
    public Long lookupCollector(String collector) {
        Long collectorMappingId = collectors.get(collector)
        if (collectorMappingId == null) {
            CollectorMapping collectorMapping = new CollectorMapping(collectorName: collector).save()
            collectors.put(collector, collectorMapping.id)
            collectorMappingId = collectorMapping.id
        }
        return collectorMappingId;
    }

    /**
     * Return the FieldMapping domain object of the given field.
     * If the field does not exist yet, it gets persisted and added to the cache.
     */
    public Long lookupField(String field) {
        Long fieldMappingId = fields.get(field)
        if (fieldMappingId == null) {
            FieldMapping fieldMapping = new FieldMapping(fieldName: field).save()
            fields.put(field, fieldMapping.id)
            fieldMappingId = fieldMapping.id
        }
        return fieldMappingId;
    }
}
