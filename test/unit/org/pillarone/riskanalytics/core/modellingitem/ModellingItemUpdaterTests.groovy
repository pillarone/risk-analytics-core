package org.pillarone.riskanalytics.core.modellingitem

import com.google.common.collect.ImmutableList
import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.*
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status

class ModellingItemUpdaterTests {

    @Test
    void testCopyP14N() {
        assert !ModellingItemUpdater.createOrUpdateModellingItem(null, new Parameterization('name'))

        ParameterizationCacheItem source = new ParameterizationCacheItem(123l, new VersionNumber("1.0"),
                'name', CoreModel, new DateTime(), new DateTime(), new Person(), new Person(), ImmutableList.copyOf([new Tag(name: 'tagName')]), true, Status.DATA_ENTRY, 200l
        )

        Parameterization result = ModellingItemUpdater.createOrUpdateModellingItem(source, null)
        assert !result.is(source)
        assert result.id == source.id
        assert result.versionNumber == source.versionNumber
        assert result.creationDate == source.creationDate
        assert result.modificationDate == source.modificationDate
        assert result.creator == source.creator
        assert result.lastUpdater == source.lastUpdater
        assert result.tags == source.tags
        assert result.valid == source.valid
        assert result.status == source.status
        assert result.dealId == source.dealId

        Parameterization target = new Parameterization('target')
        result = ModellingItemUpdater.createOrUpdateModellingItem(source, target)
        assert result.is(target)
        assert !result.is(source)
        assert result.id == source.id
        assert result.versionNumber == source.versionNumber
        assert result.creationDate == source.creationDate
        assert result.modificationDate == source.modificationDate
        assert result.creator == source.creator
        assert result.lastUpdater == source.lastUpdater
        assert result.tags == source.tags
        assert result.valid == source.valid
        assert result.status == source.status
        assert result.dealId == source.dealId
    }

    @Test
    void testCopyResultConfiguration() {
        assert !ModellingItemUpdater.createOrUpdateModellingItem(null, new ResultConfiguration('name', CoreModel))

        ResultConfigurationCacheItem source = new ResultConfigurationCacheItem(
                100l, 'name', CoreModel, new VersionNumber('1.2'), new DateTime(), new DateTime(), new Person(), new Person()
        )
        ResultConfiguration target = ModellingItemUpdater.createOrUpdateModellingItem(source, null)
        assert !target.is(source)
        assert source.name == target.name
        assert source.id == target.id
        assert source.modelClass == target.modelClass
        assert source.versionNumber?.clone() == target.versionNumber
        assert source.creationDate == target.creationDate
        assert source.modificationDate == target.modificationDate
        assert source.creator == target.creator
        assert source.lastUpdater == target.lastUpdater

        ResultConfiguration toUpdate = new ResultConfiguration('target', CoreModel)
        target = ModellingItemUpdater.createOrUpdateModellingItem(source, toUpdate)
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
        assert !ModellingItemUpdater.createOrUpdateModellingItem(null, new Simulation('name'))
        SimulationCacheItem source = new SimulationCacheItem(
                100l, 'name', new ParameterizationCacheItem(1l, 'name', null, null,), new ResultConfigurationCacheItem(1l, 'name', EmptyModel, null), ImmutableList.copyOf([new Tag(name: 'tagName')]), CoreModel, new VersionNumber('7.0'), new DateTime(), new DateTime(), new DateTime(), new DateTime(), new Person(), 0
        )

        Simulation result = ModellingItemUpdater.createOrUpdateModellingItem(source, null)
        assert !result.is(source)
        assert source.name == result.name
        assert source.id == result.id
        assert source.parameterization.id == result.parameterization.id
        assert source.resultConfiguration.id == result.template.id
        assert source.tags == result.tags
        assert source.modelClass == result.modelClass
        assert source.modelVersionNumber == result.modelVersionNumber
        assert source.end == result.end
        assert source.start == result.start
        assert source.creationDate == result.creationDate
        assert source.modificationDate == result.modificationDate
        assert source.creator == result.creator
        assert source.numberOfIterations == result.numberOfIterations

        Simulation toUpdate = new Simulation('toUpdate')
        result = ModellingItemUpdater.createOrUpdateModellingItem(source, toUpdate)
        assert !result.is(source)
        assert result.is(toUpdate)
        assert source.name == result.name
        assert source.id == result.id
        assert source.parameterization.id == result.parameterization.id
        assert source.resultConfiguration.id == result.template.id
        assert source.tags == result.tags
        assert source.modelClass == result.modelClass
        assert source.modelVersionNumber == result.modelVersionNumber
        assert source.end == result.end
        assert source.start == result.start
        assert source.creationDate == result.creationDate
        assert source.modificationDate == result.modificationDate
        assert source.creator == result.creator
        assert source.numberOfIterations == result.numberOfIterations

    }

    @Test
    void testCopyResource() {
        assert !ModellingItemUpdater.createOrUpdateModellingItem(null, new Resource('name', CoreModel))

        ResourceCacheItem source = new ResourceCacheItem(
                100l, 'name0', CoreModel, new VersionNumber('2.9'), new DateTime(), new DateTime(), new Person(), new Person(), ImmutableList.copyOf([new Tag(name: 'tagName')]), true, Status.IN_PRODUCTION
        )

        Resource result = ModellingItemUpdater.createOrUpdateModellingItem(source, null)
        assert !result.is(source)
        assert source.id == result.id
        assert source.name == result.name
        assert source.versionNumber == result.versionNumber
        assert source.creationDate == result.creationDate
        assert source.modificationDate == result.modificationDate
        assert source.creator == result.creator
        assert source.lastUpdater == result.lastUpdater
        assert source.tags == result.tags
        assert source.valid == result.valid
        assert source.status == result.status

        Resource toUpdate = new Resource('skdj', Class)
        result = ModellingItemUpdater.createOrUpdateModellingItem(source, toUpdate)
        assert !result.is(source)
        assert result.is(toUpdate)
        assert source.id == result.id
        assert source.name == result.name
        assert source.versionNumber == result.versionNumber
        assert source.creationDate == result.creationDate
        assert source.modificationDate == result.modificationDate
        assert source.creator == result.creator
        assert source.lastUpdater == result.lastUpdater
        assert source.tags == result.tags
        assert source.valid == result.valid
        assert source.status == result.status
    }
}






