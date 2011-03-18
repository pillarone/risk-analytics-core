package org.pillarone.riskanalytics.core

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterizationTag
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType

class ParameterizationDAO {

    private static Log LOG = LogFactory.getLog(ParameterizationDAO)

    String name
    String modelClassName
    ModelDAO model
    String itemVersion
    Integer periodCount

    String comment
    String periodLabels
    DateTime creationDate
    DateTime modificationDate
    Person creator
    Person lastUpdater
    boolean valid

    Status status

    Long dealId
    DateTime valuationDate

    javax.sql.DataSource dataSource

    static hasMany = [parameters: Parameter, comments: CommentDAO, issues: WorkflowCommentDAO, tags: ParameterizationTag]
    static transients = ['dataSource']


    static constraints = {
        name()
        comment(nullable: true, blank: true)
        model(nullable: true)
        periodLabels(nullable: true, blank: true, maxSize: 1000)
        creationDate nullable: true
        modificationDate nullable: true
        creator nullable: true
        lastUpdater nullable: true
        dealId(nullable: true)
        valuationDate(nullable: true)
    }

    static mapping = {
        comments(sort: "path", order: "asc")
        creator lazy: false
        lastUpdater lazy: false
        creationDate type: DateTimeMillisUserType
        modificationDate type: DateTimeMillisUserType
        valuationDate type: DateTimeMillisUserType
    }

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

}
