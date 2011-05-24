package org.pillarone.riskanalytics.core.output

import org.apache.commons.lang.builder.HashCodeBuilder
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO
import org.pillarone.riskanalytics.core.persistence.DateTimeMillisUserType
import org.pillarone.riskanalytics.core.parameter.Parameter

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

    // more to come here

    DeleteSimulationService deleteSimulationService
    javax.sql.DataSource dataSource

    static transients = ['deleteSimulationService', 'dataSource']

    static hasMany = [comments: ResultCommentDAO, runtimeParameters: Parameter]

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
    }

    static mapping = {
        comments(sort: "path", order: "asc")
        beginOfFirstPeriod type: DateTimeMillisUserType
        startTime type: DateTimeMillisUserType
        endTime type: DateTimeMillisUserType
        creationDate type: DateTimeMillisUserType
        modificationDate type: DateTimeMillisUserType
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
