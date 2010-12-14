package org.pillarone.riskanalytics.core

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterizationTag
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status

class ParameterizationDAO {

    private static Log LOG = LogFactory.getLog(ParameterizationDAO)

    String name
    String modelClassName
    String itemVersion
    Integer periodCount

    String comment
    String periodLabels
    Date creationDate
    Date modificationDate
    Person creator
    Person lastUpdater
    boolean valid

    Status status

    Long dealId

    javax.sql.DataSource dataSource

    static hasMany = [parameters: Parameter, comments: CommentDAO, issues: WorkflowCommentDAO, tags: ParameterizationTag]
    static transients = ['dataSource']


    static constraints = {
        name()
        comment(nullable: true, blank: true)
        periodLabels(nullable: true, blank: true, maxSize: 1000)
        creationDate nullable: true
        modificationDate nullable: true
        creator nullable: true
        lastUpdater nullable: true
        dealId(nullable: true)
    }

    static mapping = { comments(sort: "path", order: "asc") }

    String toString() {
        "$name v$itemVersion"
    }

    /**
     * Returns a persisted ParameterizationDAO.
     * A parameterization can be uniquely identified by Name, Model & Version.
     */
    static ParameterizationDAO find(String name, String modelClassName, String versionNumber) {
        def criteria = ParameterizationDAO.createCriteria()
        //TODO: throw exception when there is more than one result? why can modelclass be null?
        def results = criteria.list {
            eq('name', name)
            eq('itemVersion', versionNumber)
            if (modelClassName != null)
                eq('modelClassName', modelClassName)
        }
        return results.size() > 0 ? results.get(0) : null
    }

    def beforeInsert = {
        logDetails "beforeInsert", false
    }

    def beforeUpdate = {
        logDetails "beforeUpdate", false
    }

    def afterInsert = {
        logDetails "afterInsert", true
    }

    def afterUpdate = {
        logDetails "afterUpdate", true
    }

    private void logDetails(String text, boolean printStackTrace) {
        if (LOG.isTraceEnabled()) {
            try {
                LOG.trace "$text: id $id ($name $itemVersion): locking version (object): $version"
                Sql sql = new Sql(dataSource)
                List results = sql.rows("select version from parameterizationdao where id = ?", [id])
                GroovyRowResult res = results[0]
                String versionString = res == null ? "null" : res.getAt(0)
                LOG.trace "$text: id $id ($name $itemVersion): locking version (db): ${versionString}"
                if (printStackTrace) {
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace().findAll {
                        !(it.declaringClass.startsWith("sun.reflect")) &&
                                !(it.declaringClass.startsWith("groovy.lang")) &&
                                !(it.declaringClass.startsWith("java.lang.reflect")) &&
                                !(it.declaringClass.startsWith("org.codehaus.groovy"))
                    }
                    LOG.trace "Stack: ${stackTrace*.toString().join("\n")}"
                }

            } catch (Throwable t) {
                LOG.error "Exception during logging", t
            }

        }
    }

}
