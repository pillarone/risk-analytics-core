package org.pillarone.riskanalytics.core.modellingitem

import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status


class ModellingItemCopyUtilsTests {

    @Test
    void testCopyP14N() {
        def creationTime = new DateTime()
        def modTime = new DateTime()
        def creator = new Person()
        def lastUpdater = new Person()
        List tags = [new Tag(name: 'tagName')]

        Parameterization parameterization = new Parameterization('name')
        parameterization.modelClass = CoreModel
        parameterization.id = 123l
        parameterization.versionNumber = new VersionNumber("1.0")
        parameterization.creationDate = creationTime
        parameterization.modificationDate = modTime
        parameterization.creator = creator
        parameterization.lastUpdater = lastUpdater
        parameterization.tags = tags
        parameterization.valid = true
        parameterization.status = Status.DATA_ENTRY
        parameterization.dealId = 200l

        Parameterization withoutDeps = ModellingItemCopyUtils.copyModellingItem(parameterization, false)
        assert !withoutDeps.is(parameterization)
        assert withoutDeps.id == parameterization.id
        assert withoutDeps.versionNumber == parameterization.versionNumber
        assert withoutDeps.creationDate == parameterization.creationDate
        assert withoutDeps.modificationDate == parameterization.modificationDate
        assert withoutDeps.creator == parameterization.creator
        assert withoutDeps.lastUpdater == parameterization.lastUpdater
        assert withoutDeps.tags == []
        assert withoutDeps.valid == parameterization.valid
        assert withoutDeps.status == parameterization.status
        assert withoutDeps.dealId == parameterization.dealId

        Parameterization withDeps = ModellingItemCopyUtils.copyModellingItem(parameterization, true)
        assert !withDeps.is(parameterization)
        assert withDeps.id == parameterization.id
        assert withDeps.versionNumber == parameterization.versionNumber
        assert withDeps.creationDate == parameterization.creationDate
        assert withDeps.modificationDate == parameterization.modificationDate
        assert withDeps.creator == parameterization.creator
        assert withDeps.lastUpdater == parameterization.lastUpdater
        assert withDeps.tags == parameterization.tags
        assert withDeps.valid == parameterization.valid
        assert withDeps.status == parameterization.status
        assert withDeps.dealId == parameterization.dealId
    }
}
