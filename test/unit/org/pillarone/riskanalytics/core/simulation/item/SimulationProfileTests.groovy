package org.pillarone.riskanalytics.core.simulation.item

import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import models.core.CoreModel
import org.joda.time.DateTime
import org.junit.Test
import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.parameter.DoubleParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.StringParameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.user.Person

@TestMixin(DomainClassUnitTestMixin)
class SimulationProfileTests {

    @Test
    void testExists() {
        mockDomain(SimulationProfileDAO, [createValidDao('testDao', CoreModel.name)])
        assert SimulationProfile.exists('testDao', CoreModel)
        assert !SimulationProfile.exists('testDao', EmptyModel)
        assert !SimulationProfile.exists('unknown', CoreModel)
    }

    @Test
    void testCreateDaoAndDaoClass() {
        def profile = new SimulationProfile('name', CoreModel)
        def dao1 = profile.createDao()
        def dao2 = profile.createDao()
        assert dao1.class == SimulationProfileDAO
        assert dao2.class == SimulationProfileDAO
        assert dao1 != dao2

        assert profile.daoClass == SimulationProfileDAO
    }

    @Test
    void testGetCreator() {
        mockDomain(SimulationProfileDAO, [createValidDao('name', CoreModel.name)])
        def creator = SimulationProfile.getCreator('name', CoreModel)
        assert creator
        assert creator.username == 'testUser'
    }

    @Test
    void testMapToDao() {
        ResultConfigurationDAO template = createValidTemplate()
        mockDomain(ResultConfigurationDAO, [template])
        mockDomain(SimulationProfileDAO)
        mockDomain(Parameter)
        mockDomain(DoubleParameter)

        ResultConfiguration resultConfiguration = new ResultConfiguration('name', CoreModel)
        resultConfiguration.load()
        DateTime creationDate = new DateTime()
        Person creator = new Person()
        Person lastUpdater = new Person()
        DateTime modificationTime = new DateTime()
        Integer randomSeed = 345
        Integer numberOfIterations = 123
        boolean forPublic = true

        SimulationProfile profile = new SimulationProfile('profileName', CoreModel)
        profile.template = resultConfiguration
        profile.creationDate = creationDate
        profile.creator = creator
        profile.lastUpdater = lastUpdater
        profile.modificationDate = modificationTime
        profile.randomSeed = randomSeed
        profile.numberOfIterations = numberOfIterations
        profile.forPublic = forPublic
        def holder1 = ParameterHolderFactory.getHolder('path1', 0, 12d)
        def holder2 = ParameterHolderFactory.getHolder('path2', 0, 'string')
        holder1.added = true
        holder2.added = true
        profile.runtimeParameters = [holder1, holder2]

        def dao = new SimulationProfileDAO()
        dao.metaClass.addToRuntimeParameters = { Parameter param ->
            dao.runtimeParameters.add(param)
        }
        profile.mapToDao(dao)
        assert dao.name == 'profileName'
        assert dao.creationDate == creationDate
        assert dao.creator == creator
        assert dao.lastUpdater == lastUpdater
        assert dao.modificationDate == modificationTime
        assert dao.randomSeed == randomSeed
        assert dao.numberOfIterations == numberOfIterations
        assert dao.forPublic
        assert dao.runtimeParameters.size() == 2
        def param1 = dao.runtimeParameters.find { it.path == 'path1' }
        def param2 = dao.runtimeParameters.find { it.path == 'path2' }
        assert param1
        assert param1.periodIndex == 0
        assert param1.path == 'path1'
        assert param1 instanceof DoubleParameter
        assert param2
        assert param2.periodIndex == 0
        assert param2.path == 'path2'
        assert param2 instanceof StringParameter
    }

    @Test(expected = UnsupportedOperationException)
    void testGetPeriodCount() {
        new SimulationProfile('a', CoreModel).periodCount
    }

    @Test(expected = UnsupportedOperationException)
    void testAddComponent() {
        new SimulationProfile('a', CoreModel).addComponent('a', null)
    }

    @Test(expected = UnsupportedOperationException)
    void testCopyComponent() {
        new SimulationProfile('a', CoreModel).copyComponent('old', 'new', null, true)
    }

    @Test(expected = UnsupportedOperationException)
    void testRenameComponent() {
        new SimulationProfile('a', CoreModel).renameComponent('old', 'new', null)
    }

    @Test(expected = UnsupportedOperationException)
    void testRemoveComponent() {
        new SimulationProfile('a', CoreModel).removeComponent('b')
    }

    @Test
    void testLoadFromDb() {
        SimulationProfileDAO persistent = createValidDao('name', createValidTemplate(), CoreModel)
        mockDomain(SimulationProfileDAO, [persistent])
        mockDomain(ResultConfigurationDAO)
        def toLoad = new SimulationProfile('name', CoreModel)
        assert toLoad.loadFromDB().id == persistent.id
        toLoad.load()
        assert toLoad.id == persistent.id
    }

    @Test
    void testMapFromDao() {
        SimulationProfileDAO persistent = createValidDao('name', createValidTemplate(), CoreModel)
        persistent.numberOfIterations = 1
        persistent.randomSeed = 2
        persistent.creationDate = new DateTime()
        persistent.creator = new Person()
        persistent.lastUpdater = new Person()
        persistent.modificationDate = new DateTime()
        persistent.forPublic = true
        persistent.runtimeParameters = [
                new DoubleParameter(path: 'pathDouble', periodIndex: 0, doubleValue: 12d),
                new StringParameter(path: 'pathString', periodIndex: 0, parameterValue: 'hula')
        ]
        def profile = new SimulationProfile('name', CoreModel)
        profile.mapFromDao(persistent, true)
        assert profile.modelClass == CoreModel
        assert profile.template.modelClass == CoreModel
        assert profile.template.versionNumber.toString() == persistent.template.itemVersion
        assert profile.numberOfIterations == 1
        assert profile.creationDate == persistent.creationDate
        assert profile.forPublic == persistent.forPublic
        assert profile.creator == persistent.creator
        assert profile.lastUpdater == persistent.lastUpdater
        assert profile.modificationDate == persistent.modificationDate
        assert profile.randomSeed == 2
        assert profile.runtimeParameters.size() == 2
        assert profile.getParameterHolder('pathDouble', 0).businessObject == 12d
        assert profile.getParameterHolder('pathString', 0).businessObject == 'hula'
    }

    private static SimulationProfileDAO createValidDao(String name, String modelClassName = CoreModel.name) {
        new SimulationProfileDAO(
                name: name,
                creator: new Person(username: 'testUser'),
                creationDate: new DateTime(),
                modelClassName: modelClassName,
                template: new ResultConfigurationDAO(),
        )
    }

    private static SimulationProfileDAO createValidDao(String name, ResultConfigurationDAO template, Class modelClass) {
        new SimulationProfileDAO(
                name: name,
                modelClassName: modelClass.name,
                creator: new Person(username: 'testUser'),
                creationDate: new DateTime(),
                template: template,
        )
    }

    private static ResultConfigurationDAO createValidTemplate() {
        new ResultConfigurationDAO(
                name: 'name',
                itemVersion: new VersionNumber('1.0'),
                modelClassName: CoreModel.name
        )
    }
}
