package org.pillarone.riskanalytics.core.output

import org.apache.commons.lang.builder.HashCodeBuilder
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.SimulationTag
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.util.DatabaseUtils
import org.pillarone.riskanalytics.core.user.Person

class SimulationRun {

    String name
    ParameterizationDAO parameterization
    ResultConfigurationDAO resultConfiguration
    String model
    int periodCount
    int iterations
    Integer randomSeed
    Boolean toBeDeleted = false

    String comment
    DateTime startTime
    DateTime endTime
    ModelDAO usedModel
    DateTime beginOfFirstPeriod
    DateTime creationDate
    DateTime modificationDate
    Person creator

    // more to come here

    javax.sql.DataSource dataSource

    static transients = ['deleteSimulationService', 'dataSource']

    static hasMany = [comments: ResultCommentDAO, runtimeParameters: Parameter, tags: SimulationTag]

    static constraints = {
        name unique: 'model'
        comment(nullable: true, maxSize: 512)
        startTime nullable: true
        endTime nullable: true
        creationDate nullable: true
        modificationDate nullable: true
        beginOfFirstPeriod nullable: true
        randomSeed nullable: true
        toBeDeleted nullable: true
        usedModel nullable: true
        parameterization nullable: true
        resultConfiguration nullable: true
        creator nullable: true
    }

    static mapping = {
        comments(sort: "path", order: "asc")
        beginOfFirstPeriod type: DateTimeMillisUserType
        startTime type: DateTimeMillisUserType
        endTime type: DateTimeMillisUserType
        creationDate type: DateTimeMillisUserType
        creator lazy: false
        modificationDate type: DateTimeMillisUserType
        if (DatabaseUtils.isOracleDatabase()) {
            comment(column: 'comment_value')
            runtimeParameters(joinTable: [name: 'run_parameter', key: 'run_id', column: 'parameter_id'])
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof SimulationRun) {
            return obj.id == id
        }
        return false
    }

    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder()
        builder.append(id)
        builder.toHashCode()
    }


}
