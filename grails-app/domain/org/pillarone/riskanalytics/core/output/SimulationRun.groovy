package org.pillarone.riskanalytics.core.output

import org.apache.commons.lang.builder.HashCodeBuilder
import org.joda.time.DateTime
import org.joda.time.contrib.hibernate.PersistentDateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO

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
    Date startTime
    Date endTime
    String modelVersionNumber // TODO (Sep 21, 2009, msh): why not reference a ModelDAO ?
    DateTime beginOfFirstPeriod
    Date creationDate
    Date modificationDate

    // more to come here

    DeleteSimulationService deleteSimulationService
    javax.sql.DataSource dataSource

    static transients = ['deleteSimulationService', 'dataSource']

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
        parameterization nullable: true
        resultConfiguration nullable: true
    }

    static mapping = {
        beginOfFirstPeriod type: PersistentDateTime
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
