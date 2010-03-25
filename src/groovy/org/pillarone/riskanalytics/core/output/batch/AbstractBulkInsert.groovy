package org.pillarone.riskanalytics.core.output.batch

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.SingleValueResult

abstract class AbstractBulkInsert {
    public static final String DEFAULT_COLLECTOR_NAME = "aggregated"
    protected static final Log LOG = LogFactory.getLog(AbstractBulkInsert)

    File tempFile
    BufferedWriter writer
    SimulationRun simulationRun
    boolean initialized = false

    private void init() {
        if (!initialized) {
            String filename = "${FileConstants.TEMP_FILE_DIRECTORY}${File.separatorChar}${simulationRun.id}"
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
}
