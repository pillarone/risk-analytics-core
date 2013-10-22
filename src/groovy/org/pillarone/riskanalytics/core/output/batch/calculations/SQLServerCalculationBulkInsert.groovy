package org.pillarone.riskanalytics.core.output.batch.calculations

import grails.util.Holders
import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.apache.maven.artifact.ant.shaded.FileUtils

/**
 * Allianz Risk Transfer  ATOM
 * User: bzetterstrom
 */
@CompileStatic
class SQLServerCalculationBulkInsert extends AbstractCalculationsBulkInsert {
    static final String BULK_INSERT_TEMP_DIR_KEY = "bulkInsertTempDir"

    @Override
    protected void save() {
        long time = System.currentTimeMillis()
        Sql sql = new Sql(simulationRun.dataSource)
        String resultFilePath = getResultFilePath()
        String formatFilePath = getFormatFilePath()
        String query = "BULK INSERT post_simulation_calculation FROM '" + resultFilePath + "' WITH (FORMATFILE = '" + formatFilePath + "', KEEPNULLS)"//.replaceAll('\\\\', '/')
        int numberOfResults = sql.executeUpdate(query)
        time = System.currentTimeMillis() - time
        LOG.info("${numberOfResults} post_simulation_calculation rows saved in ${time} ms");
        sql.close()
    }

    @Override
    protected void writeResult(final List values) {
        values.add(0, 0) //dummy id
        values.add(0, 0) //version
        writer.writeLine(values.join(","))
    }

    @Override
    protected String getNull() {
        return ""; // empty value should mean NULL when the flag WITH(KEEPNULLS) is used
    }

    /**
     * If running with a non-local database, we need to ensure that the temp directory and format files are accessible
     * This method checks if a shared path has been specified(runtime param or config)
     * @return the
     */
    private String getResultFilePath() {
        String confgd = getResultFileTempDir()
        if (confgd) {
            String result = "\\\\" + InetAddress.getLocalHost().getHostName() + "\\" + confgd + "\\" + tempFile.name
            File resFile = new File(result)
            assert resFile.exists() //, "Hint: Is the temp directory shared so the DB can read it?"
            return result
        } else {
            return tempFile.getAbsolutePath()
        }
    }

    private String getFormatFilePath() {
        String confgd = getResultFileTempDir()
        File localFile = new File(getClass().getResource("/post_simulation_calculation_formatfile.xml").toURI())
        if (confgd) {
            String sharedPath = "\\\\" + InetAddress.getLocalHost().getHostName() + "\\" + confgd
            //Ensure tmp dir is valid etc etc
            File tmpDir = new File(sharedPath)
            assert tmpDir.isDirectory(): sharedPath + " is not a valid directory"
            File formatFile = tmpDir.listFiles().find { File it -> it.name == "post_simulation_calculation_formatfile.xml" }
            if (!formatFile) {
                //The format file needs to be accessible by the database, so copy it to the temp dir.
                assert tmpDir.canWrite(): "Can not write to the remote directory " + tmpDir + "!"
                formatFile = new File(tmpDir.absolutePath + "/post_simulation_calculation_formatfile.xml")
                FileUtils.copyFile(localFile, formatFile)
            }
            return formatFile.getAbsolutePath()
        } else {
            return localFile.getAbsolutePath()
        }
    }

    /**
     * Returns the shared directory where result files end up.
     * This should only be present(via system property or config) ir using a database on a different host than main P1 server
     * @return the shared directory name on current host where result files end up, or <code>null</code> if no such directory is specified
     */
    private String getResultFileTempDir() {
        return System.getProperty(BULK_INSERT_TEMP_DIR_KEY) ? System.getProperty(BULK_INSERT_TEMP_DIR_KEY) : Holders.config.get(BULK_INSERT_TEMP_DIR_KEY)
    }
}


