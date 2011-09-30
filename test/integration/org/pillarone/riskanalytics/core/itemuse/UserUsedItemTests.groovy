package org.pillarone.riskanalytics.core.itemuse

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.user.itemuse.item.UserUsedParameterization
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel

/**
 * Created by IntelliJ IDEA.
 * User: bzetterstrom
 * Date: 28/06/11
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */
class UserUsedItemTests extends GroovyTestCase {
    void testUserUsedParameterization() {
        UserUsedParameterization userUsedParameterization = new UserUsedParameterization()
        userUsedParameterization.time = new DateTime();
        Parameterization parameterization = new Parameterization(versionNumber: new VersionNumber('1'), name: 'testUserUsedParameterization', modelClass: EmptyModel)
        parameterization.save()
        userUsedParameterization.parameterization = parameterization.dao
        userUsedParameterization.save();

        assertNotNull(UserUsedParameterization.findByParameterization (ParameterizationDAO.findByName("testUserUsedParameterization")))
    }
}
