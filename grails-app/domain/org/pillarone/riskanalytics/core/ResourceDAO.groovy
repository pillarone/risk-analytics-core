package org.pillarone.riskanalytics.core

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.comment.ResourceCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResourceTag
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowResourceCommentDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.util.DatabaseUtils
import org.pillarone.riskanalytics.core.workflow.Status

class ResourceDAO {

    private static Log LOG = LogFactory.getLog(ParameterizationDAO)

    String name
    String resourceClassName
    String itemVersion
    String comment
    DateTime creationDate
    DateTime modificationDate
    Person creator
    Person lastUpdater
    boolean valid
    Status status

    static hasMany = [parameters: Parameter, comments: ResourceCommentDAO, issues: WorkflowResourceCommentDAO, tags: ResourceTag]


    static constraints = {
        itemVersion(blank: false)
        comment(nullable: true, blank: true)
        creationDate nullable: true
        modificationDate nullable: true
        creator nullable: true
        lastUpdater nullable: true
    }

    static mapping = {
        comments(sort: "path", order: "asc")
        creator lazy: false
        lastUpdater lazy: false
        creationDate type: DateTimeMillisUserType
        modificationDate type: DateTimeMillisUserType
        if (DatabaseUtils.isOracleDatabase()) {
            comment(column: 'comment_value')
            parameters(joinTable:[name: 'dao_parameter', key:'dao_id', column: 'parameter_id'])
        }
    }

    String toString() {
        "$name v$itemVersion"
    }
}
