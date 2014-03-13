package org.pillarone.riskanalytics.core

import grails.test.mixin.TestFor
import models.core.CoreModel
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserSettings
import spock.lang.Specification

@TestFor(SimulationProfileDAO)
class SimulationProfileDAOTests extends Specification {
    void 'test constraints on name'() {
        given:
        mockForConstraintsTests SimulationProfileDAO, [new SimulationProfileDAO(name: 'thisExistsForCoreModel', modelClassName: CoreModel.name)]

        when: 'the name is null'
        def subject = new SimulationProfileDAO()
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['name'] == 'nullable'

        when: 'the name is blank'
        subject.name = ''
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['name'] == 'blank'

        when: 'the name exists for CoreModel'
        subject.modelClassName = CoreModel.name
        subject.name = 'thisExistsForCoreModel'
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['name'] == 'unique'

        when: 'the name does exist but for EmptyModel'
        subject.modelClassName = EmptyModel.name
        subject.name = 'thisExistsForCoreModel'
        then: 'we have no errors on field name'
        !subject.validate()
        !subject.errors['name']
    }

    void 'test constraints on modelClassName'() {
        given:
        mockForConstraintsTests SimulationProfileDAO

        when: 'the modelClassName is null'
        def subject = new SimulationProfileDAO()
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['modelClassName'] == 'nullable'

        when: 'the modelClassName is blank'
        subject.modelClassName = ''
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['modelClassName'] == 'blank'
    }

    void 'test constraints on creationDate'() {
        given:
        mockForConstraintsTests SimulationProfileDAO

        when: 'the creationDate is null'
        def subject = new SimulationProfileDAO()
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['creationDate'] == 'nullable'
    }


    void 'test successful validation'() {
        given:
        mockForConstraintsTests SimulationProfileDAO

        when: 'everything is ok'
        def subject = new SimulationProfileDAO(
                name: 'uniqueNameAndNotBlank',
                modelClassName: CoreModel.name,
                template: new ResultConfigurationDAO(),
                randomSeed: 0,
                creationDate: new DateTime(),
        )

        then: 'validation should succeed'
        subject.validate()
    }

    void 'test named query withModelClass'() {
        given:
        mockDomain(SimulationProfileDAO)
        mockDomain(Person)

        Person user1 = new Person(username: 'user1', password: 'user1', settings: new UserSettings(language: 'de')).save()
        Person user2 = new Person(username: 'user2', password: 'user2', settings: new UserSettings(language: 'de')).save()

        def dao1 = createValidDao('core1', CoreModel, user1, false).save()
        def dao2 = createValidDao('core2', CoreModel, user2, true).save()
        def dao3 = createValidDao('empty1', EmptyModel, user1, true).save()
        def dao4 = createValidDao('empty2', EmptyModel, null, false).save()

        when: 'we query all with modelClass CoreModel'
        Set resultSet = SimulationProfileDAO.withModelClass(CoreModel).list()

        then: 'we should get all with CoreModel'
        [dao1, dao2] as Set == resultSet

        when: 'we query all with modelClass EmptyModel'
        resultSet = SimulationProfileDAO.withModelClass(EmptyModel).list()

        then: 'we should get all with EmptyModel'
        [dao3, dao4] as Set == resultSet

//        when: 'we query all with creator user1'
//        resultSet = SimulationProfileDAO.withCreator(user1).list()
//
//        then: 'we should find all with creator user1'
//        assert [dao1, dao3] as Set == resultSet
//
//        when: 'we query all with empty creator'
//        resultSet = SimulationProfileDAO.withCreator(null).list()
//
//        then: 'we should find all with empty creator'
//        assert [dao4] as Set == resultSet
//
//        when: 'we query with null creator or for public'
//        resultSet = SimulationProfileDAO.withCreatorOrForPublic(null).list()
//
//        then: 'we should get one result'
//        [dao2, dao3, dao4] as Set == resultSet
//
//        when: 'we query with user1 or for public'
//        resultSet = SimulationProfileDAO.withCreatorOrForPublic(user1).list()
//
//        then: 'we should get all daos with user1 or forPublic'
//        assert [dao1, dao2, dao3] as Set == resultSet
//
//        when: 'we query with user2 or for public'
//        resultSet = SimulationProfileDAO.withCreatorOrForPublic(user2).list()
//
//        then: 'we should get all daos with user1 or forPublic'
//        assert [dao2, dao3] as Set == resultSet
    }

    private
    static SimulationProfileDAO createValidDao(String name, Class modelClass, Person person, boolean forPublic) {
        new SimulationProfileDAO(
                name: name,
                modelClassName: modelClass.name,
                creator: person,
                creationDate: new DateTime(),
                forPublic: forPublic
        )
    }
}
