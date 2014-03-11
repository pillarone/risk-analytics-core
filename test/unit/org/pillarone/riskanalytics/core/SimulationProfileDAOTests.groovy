package org.pillarone.riskanalytics.core

import grails.test.mixin.TestFor
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
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

    void 'test successful validation'() {
        given:
        mockForConstraintsTests SimulationProfileDAO

        when: 'everything is ok'
        def subject = new SimulationProfileDAO(
                name: 'uniqueNameAndNotBlank',
                template: new ResultConfigurationDAO(),
                randomSeed: 0
        )

        then: 'validation should succeed'
        subject.validate()

        when: 'random seed is null'
        subject = new SimulationProfileDAO(
                name: 'uniqueNameAndNotBlank',
                template: new ResultConfigurationDAO(),
        )

        then: 'validation should succeed'
        subject.validate()
    }


}
