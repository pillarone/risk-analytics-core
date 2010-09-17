package org.pillarone.riskanalytics.core.workflow

import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import static org.pillarone.riskanalytics.core.workflow.Status.*
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class StatusChangeServiceTests extends GroovyTestCase {

    StatusChangeService statusChangeService

    void testToDataEntryFromNone() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertNotSame newParameterization, parameterization

        assertEquals NONE, parameterization.status

        assertEquals DATA_ENTRY, newParameterization.status
        assertEquals "R1", newParameterization.versionNumber.toString()
    }

    void testToProduction() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, IN_PRODUCTION)
        assertSame newParameterization, parameterization

        assertEquals IN_PRODUCTION, parameterization.status
        assertEquals "R1", parameterization.versionNumber.toString()
    }

    void testToInReview() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = DATA_ENTRY
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, IN_REVIEW)
        assertSame newParameterization, parameterization

        assertEquals IN_REVIEW, parameterization.status
        assertEquals "R1", parameterization.versionNumber.toString()
    }

    void testToDataEntryFromInReview() {
        Parameterization parameterization = new Parameterization("name")
        parameterization.status = IN_REVIEW
        parameterization.versionNumber = new VersionNumber("R1")
        parameterization.modelClass = EmptyModel
        parameterization.periodCount = 0
        parameterization.save()

        Parameterization newParameterization = statusChangeService.changeStatus(parameterization, DATA_ENTRY)
        assertNotSame newParameterization, parameterization

        assertEquals REJECTED, parameterization.status

        assertEquals DATA_ENTRY, newParameterization.status
        assertEquals "R2", newParameterization.versionNumber.toString()
    }
}
