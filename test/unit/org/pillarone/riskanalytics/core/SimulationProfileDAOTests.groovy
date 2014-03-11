package org.pillarone.riskanalytics.core

import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.user.Person
import spock.lang.Specification

@TestFor(SimulationProfileDAO)
class SimulationProfileDAOTests extends Specification {
    void 'test constraints on name'() {
        given:
        mockForConstraintsTests SimulationProfileDAO, [new SimulationProfileDAO(name: 'thisExists')]

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

        when: 'the name exist'
        subject.name = 'thisExists'
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['name'] == 'unique'
    }

    void 'test constraints on template'() {
        given:
        mockForConstraintsTests SimulationProfileDAO

        when: 'the template is null'
        def subject = new SimulationProfileDAO()
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['template'] == 'nullable'
    }

    void 'test constraints on creator'() {
        given:
        mockForConstraintsTests SimulationProfileDAO

        when: 'the creator is null'
        def subject = new SimulationProfileDAO()
        then: 'validation should fail'
        !subject.validate()
        subject.hasErrors()
        subject.errors['creator'] == 'nullable'
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
                template: new ResultConfigurationDAO(),
                randomSeed: 0,
                creationDate: new DateTime(),
                creator: new Person()
        )

        then: 'validation should succeed'
        subject.validate()
    }
}
