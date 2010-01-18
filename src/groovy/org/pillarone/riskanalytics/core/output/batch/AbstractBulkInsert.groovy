package org.pillarone.riskanalytics.core.output.batch

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.output.*

abstract class AbstractBulkInsert {
    static String DEFAULT_COLLECTOR_NAME = "aggregated"
    protected static Log LOG = LogFactory.getLog(AbstractBulkInsert)

    File tempFile
    BufferedWriter writer
    SimulationRun simulationRun
    boolean initialized = false

    private void init() {
        if (!initialized) {
            URL url = getClass().getResource('/data')
            String currentDirectory = new File(url.toURI()).path
            String filename = "${currentDirectory}${File.separatorChar}${simulationRun.id}"
            LOG.info("Temp file at: $filename")

            tempFile = new File(filename)
            tempFile.delete()
            writer = tempFile.newWriter(true)
            initialized = true
        }
    }

    void setSimulationRun(SimulationRun simulationRun) {
        this.@simulationRun = simulationRun
        init()
    }

    void addResults(List results) {
        List values = []
        for (SingleValueResult result in results) {
            values << result.simulationRun.id
            values << result.period
            values << result.iteration
            values << result.path.id
            values << result.field.id
            values << result.collector.id
            values << result.value
            writeResult(values)
            values.clear()
        }
        writer.flush()
    }

    abstract protected void writeResult(List values)

    final void saveToDB() {
        if (initialized) {
            writer.close()
            save()
            tempFile.delete()
        }
    }

    void reset() {
        if (initialized) {
            writer.close()
            tempFile.delete()
            initialized = false
            simulationRun = null
        }
    }

    abstract protected void save()


    static AbstractBulkInsert getBulkInsertInstance() {
        Class bulkClass = ApplicationHolder.application?.config?.batchInsert
        if (!bulkClass) {
            return new GenericBulkInsert()
        }
        return AbstractBulkInsert.classLoader.loadClass(bulkClass.name).newInstance()
    }

    Map fieldIds = [:]
    Map pathIds = [:]
    Map collectorIds = [:]


    public long getFieldId(String field) {
        field = field.toString()
        if (fieldIds[field] != null) {
            return fieldIds[field]
        }
        FieldMapping fieldObject = FieldMapping.findByFieldName(field)
        if (fieldObject == null) {
            fieldObject = new FieldMapping(fieldName: field)
            fieldObject.save()
            fieldIds[field] = fieldObject.id
            return fieldObject.id
        } else {
            fieldIds[field] = fieldObject.id
            return fieldObject.id
        }
    }



    public long getPathId(String path) {
        path = path.toString()
        if (pathIds[path] != null) {
            return pathIds[path]
        }
        PathMapping pathObject = PathMapping.findByPathName(path)
        if (pathObject == null) {
            pathObject = new PathMapping(pathName: path)
            pathObject.save()
            pathIds[path] = pathObject.id
            return pathObject.id
        } else {
            pathIds[path] = pathObject.id
            return pathObject.id
        }
    }



    public long getCollectorId(String collectorName) {
        collectorName = collectorName.toString()
        if (collectorIds[collectorName] != null) {
            return collectorIds[collectorName]
        }
        CollectorMapping collectorObject = CollectorMapping.findByCollectorName(collectorName)
        if (collectorObject == null) {
            collectorObject = new CollectorMapping(collectorName: collectorName)
            collectorObject.save()
            collectorIds[collectorName] = collectorObject.id
            return collectorObject.id
        } else {
            collectorIds[collectorName] = collectorObject.id
            return collectorObject.id
        }
    }
}
