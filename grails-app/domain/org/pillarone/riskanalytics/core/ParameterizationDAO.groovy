package org.pillarone.riskanalytics.core

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterizationTag
import org.pillarone.riskanalytics.core.parameter.comment.ParameterizationCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.util.DatabaseUtils
import org.pillarone.riskanalytics.core.workflow.Status

import javax.sql.DataSource

class ParameterizationDAO {

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

    DataSource dataSource

    static hasMany = [parameters: Parameter, comments: ParameterizationCommentDAO, issues: WorkflowCommentDAO, tags: ParameterizationTag]
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
        comments(sort: 'path', order: 'asc')
        creator lazy: false
        lastUpdater lazy: false
        creationDate type: DateTimeMillisUserType
        modificationDate type: DateTimeMillisUserType
        valuationDate type: DateTimeMillisUserType
        if (DatabaseUtils.isOracleDatabase()) {
            comment(column: 'comment_value')
            parameters(joinTable: [name: 'dao_parameter', key: 'dao_id', column: 'parameter_id'])
        }
    }

    String toString() {
        "$name v$itemVersion"
    }

    /**
     * Returns a persisted ParameterizationDAO.
     * A parameterization can be uniquely identified by Name, Model & Version.
     */
    static ParameterizationDAO find(String name, String modelClassName, String versionNumber) {
        //TODO: throw exception when there is more than one result? why can modelclass be null?
        def results = createCriteria().list {
            eq('name', name)
            eq('itemVersion', versionNumber)
            if (modelClassName != null) {
                eq('modelClassName', modelClassName)
            }
        }
        results.size() > 0 ? results.get(0) : null
    }

}
