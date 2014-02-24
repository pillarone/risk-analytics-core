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

        assert !ModellingItemCopyUtils.copyModellingItem(null, new Parameterization('target'))

        def creationTime = new DateTime()
        def modTime = new DateTime()
        def creator = new Person()
        def lastUpdater = new Person()
        List tags = [new Tag(name: 'tagName')]

        Parameterization source = new Parameterization('name')
        source.modelClass = CoreModel
        source.id = 123l
        source.versionNumber = new VersionNumber("1.0")
        source.creationDate = creationTime
        source.modificationDate = modTime
        source.creator = creator
        source.lastUpdater = lastUpdater
        source.tags = tags
        source.valid = true
        source.status = Status.DATA_ENTRY
        source.dealId = 200l

        Parameterization withoutDeps = ModellingItemCopyUtils.copyModellingItem(source, null, false)
        assert !withoutDeps.is(source)
        assert withoutDeps.id == source.id
        assert withoutDeps.versionNumber == source.versionNumber
        assert withoutDeps.creationDate == source.creationDate
        assert withoutDeps.modificationDate == source.modificationDate
        assert withoutDeps.creator == source.creator
        assert withoutDeps.lastUpdater == source.lastUpdater
        assert withoutDeps.tags == []
        assert withoutDeps.valid == source.valid
        assert withoutDeps.status == source.status
        assert withoutDeps.dealId == source.dealId

        Parameterization withDeps = ModellingItemCopyUtils.copyModellingItem(source, null, true)
        assert !withDeps.is(source)
        assert withDeps.id == source.id
        assert withDeps.versionNumber == source.versionNumber
        assert withDeps.creationDate == source.creationDate
        assert withDeps.modificationDate == source.modificationDate
        assert withDeps.creator == source.creator
        assert withDeps.lastUpdater == source.lastUpdater
        assert withDeps.tags == source.tags
        assert withDeps.valid == source.valid
        assert withDeps.status == source.status
        assert withDeps.dealId == source.dealId

        Parameterization target = new Parameterization('target')
        withoutDeps = ModellingItemCopyUtils.copyModellingItem(source, target, false)
        assert withoutDeps.is(target)
        assert !withoutDeps.is(source)
        assert withoutDeps.id == source.id
        assert withoutDeps.versionNumber == source.versionNumber
        assert withoutDeps.creationDate == source.creationDate
        assert withoutDeps.modificationDate == source.modificationDate
        assert withoutDeps.creator == source.creator
        assert withoutDeps.lastUpdater == source.lastUpdater
        assert withoutDeps.tags == []
        assert withoutDeps.valid == source.valid
        assert withoutDeps.status == source.status
        assert withoutDeps.dealId == source.dealId

        target = new Parameterization('target')
        withDeps = ModellingItemCopyUtils.copyModellingItem(source, target, true)
        assert withDeps.is(target)
        assert !withDeps.is(source)
        assert withDeps.id == source.id
        assert withDeps.versionNumber == source.versionNumber
        assert withDeps.creationDate == source.creationDate
        assert withDeps.modificationDate == source.modificationDate
        assert withDeps.creator == source.creator
        assert withDeps.lastUpdater == source.lastUpdater
        assert withDeps.tags == source.tags
        assert withDeps.valid == source.valid
        assert withDeps.status == source.status
        assert withDeps.dealId == source.dealId
    }
}
