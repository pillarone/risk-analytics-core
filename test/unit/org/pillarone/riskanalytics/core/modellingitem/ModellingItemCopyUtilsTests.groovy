package org.pillarone.riskanalytics.core.modellingitem

import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.*
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status

class ModellingItemCopyUtilsTests {

    @Test
    void testCopyP14N() {

        assert !ModellingItemCopyUtils.copyModellingItem(null, new Parameterization('target'))

        Parameterization source = new Parameterization('name')
        source.modelClass = CoreModel
        source.id = 123l
        source.versionNumber = new VersionNumber("1.0")
        source.creationDate = new DateTime()
        source.modificationDate = new DateTime()
        source.creator = new Person()
        source.lastUpdater = new Person()
        source.tags = [new Tag(name: 'tagName')]
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

    @Test
    void testCopyResultConfiguration() {
        assert !ModellingItemCopyUtils.copyModellingItem(null, new ResultConfiguration('test'))

        ResultConfiguration source = new ResultConfiguration('name')
        source.id = 100l
        source.modelClass = CoreModel
        source.versionNumber = new VersionNumber('1.2')
        source.creationDate = new DateTime()
        source.modificationDate = new DateTime()
        source.creator = new Person()
        source.lastUpdater = new Person()

        ResultConfiguration target = ModellingItemCopyUtils.copyModellingItem(source, null)
        assert !target.is(source)
        assert source.name == target.name
        assert source.id == target.id
        assert source.modelClass == target.modelClass
        assert source.versionNumber?.clone() == target.versionNumber
        assert source.creationDate == target.creationDate
        assert source.modificationDate == target.modificationDate
        assert source.creator == target.creator
        assert source.lastUpdater == target.lastUpdater

        ResultConfiguration toUpdate = new ResultConfiguration('target')
        target = ModellingItemCopyUtils.copyModellingItem(source, toUpdate)
        assert !target.is(source)
        assert target.is(toUpdate)
        assert source.name == target.name
        assert source.id == target.id
        assert source.modelClass == target.modelClass
        assert source.versionNumber?.clone() == target.versionNumber
        assert source.creationDate == target.creationDate
        assert source.modificationDate == target.modificationDate
        assert source.creator == target.creator
        assert source.lastUpdater == target.lastUpdater
    }

    @Test
    void testCopySimulation() {
        assert !ModellingItemCopyUtils.copyModellingItem(null, new Simulation('name'))
        Simulation source = new Simulation('source')
        source.id = 100l
        source.parameterization = new Parameterization('p14n')
        source.parameterization.modelClass = CoreModel
        source.template = new ResultConfiguration('resConf')
        source.template.modelClass = CoreModel
        source.tags = [new Tag(name: 'tagName')]
        source.modelClass = CoreModel
        source.modelVersionNumber = new VersionNumber('7.0')
        source.end = new DateTime()
        source.start = new DateTime()
        source.creationDate = new DateTime()
        source.modificationDate = new DateTime()
        source.creator = new Person()
        source.numberOfIterations = 2

        Simulation withoutDeps = ModellingItemCopyUtils.copyModellingItem(source, null, false)

        assert !withoutDeps.is(source)
        assert source.name == withoutDeps.name
        assert source.id == withoutDeps.id
        assert !withoutDeps.parameterization
        assert !withoutDeps.template
        assert withoutDeps.tags == []
        assert source.modelClass == withoutDeps.modelClass
        assert source.modelVersionNumber == withoutDeps.modelVersionNumber
        assert source.end == withoutDeps.end
        assert source.start == withoutDeps.start
        assert source.creationDate == withoutDeps.creationDate
        assert source.modificationDate == withoutDeps.modificationDate
        assert source.creator == withoutDeps.creator
        assert source.numberOfIterations == withoutDeps.numberOfIterations

        Simulation withDeps = ModellingItemCopyUtils.copyModellingItem(source, null, true)
        assert !withDeps.is(source)
        assert source.name == withDeps.name
        assert source.id == withDeps.id
        assert source.parameterization == withDeps.parameterization
        assert source.template == withDeps.template
        assert source.tags == withDeps.tags
        assert source.modelClass == withDeps.modelClass
        assert source.modelVersionNumber == withDeps.modelVersionNumber
        assert source.end == withDeps.end
        assert source.start == withDeps.start
        assert source.creationDate == withDeps.creationDate
        assert source.modificationDate == withDeps.modificationDate
        assert source.creator == withDeps.creator
        assert source.numberOfIterations == withDeps.numberOfIterations

        Simulation toUpdate = new Simulation('toUpdate')
        withDeps = ModellingItemCopyUtils.copyModellingItem(source, toUpdate, true)
        assert !withDeps.is(source)
        assert withDeps.is(toUpdate)
        assert source.name == withDeps.name
        assert source.id == withDeps.id
        assert source.parameterization == withDeps.parameterization
        assert source.template == withDeps.template
        assert source.tags == withDeps.tags
        assert source.modelClass == withDeps.modelClass
        assert source.modelVersionNumber == withDeps.modelVersionNumber
        assert source.end == withDeps.end
        assert source.start == withDeps.start
        assert source.creationDate == withDeps.creationDate
        assert source.modificationDate == withDeps.modificationDate
        assert source.creator == withDeps.creator
        assert source.numberOfIterations == withDeps.numberOfIterations

    }

    @Test
    void testCopyResource() {
        assert !ModellingItemCopyUtils.copyModellingItem(null, new Resource('name', CoreModel))

        Resource source = new Resource('name', CoreModel)
        source.id = 100l
        source.versionNumber = new VersionNumber('2.9')
        source.creationDate = new DateTime()
        source.modificationDate = new DateTime()
        source.creator = new Person()
        source.lastUpdater = new Person()
        source.tags = [new Tag(name: 'tagName')]
        source.valid = false
        source.status = Status.IN_PRODUCTION

        Resource withoutDeps = ModellingItemCopyUtils.copyModellingItem(source, null, false)
        assert !withoutDeps.is(source)
        assert source.id == withoutDeps.id
        assert source.name == withoutDeps.name
        assert source.versionNumber == withoutDeps.versionNumber
        assert source.creationDate == withoutDeps.creationDate
        assert source.modificationDate == withoutDeps.modificationDate
        assert source.creator == withoutDeps.creator
        assert source.lastUpdater == withoutDeps.lastUpdater
        assert withoutDeps.tags == []
        assert source.valid == withoutDeps.valid
        assert source.status == withoutDeps.status


        Resource withDeps = ModellingItemCopyUtils.copyModellingItem(source, null, true)
        assert !withDeps.is(source)
        assert source.id == withDeps.id
        assert source.name == withDeps.name
        assert source.versionNumber == withDeps.versionNumber
        assert source.creationDate == withDeps.creationDate
        assert source.modificationDate == withDeps.modificationDate
        assert source.creator == withDeps.creator
        assert source.lastUpdater == withDeps.lastUpdater
        assert source.tags == withDeps.tags
        assert source.valid == withDeps.valid
        assert source.status == withDeps.status

        Resource toUpdate = new Resource('skdj', Class)
        withDeps = ModellingItemCopyUtils.copyModellingItem(source, toUpdate, true)
        assert !withDeps.is(source)
        assert withDeps.is(toUpdate)
        assert source.id == withDeps.id
        assert source.name == withDeps.name
        assert source.versionNumber == withDeps.versionNumber
        assert source.creationDate == withDeps.creationDate
        assert source.modificationDate == withDeps.modificationDate
        assert source.creator == withDeps.creator
        assert source.lastUpdater == withDeps.lastUpdater
        assert source.tags == withDeps.tags
        assert source.valid == withDeps.valid
        assert source.status == withDeps.status
    }
}






