package org.pillarone.riskanalytics.core

import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterObjectParameter
import org.pillarone.riskanalytics.core.parameter.StringParameter
import org.pillarone.riskanalytics.core.parameter.EnumParameter
import models.core.CoreModel


class ParameterizationDAOTests extends GroovyTestCase {

    void testInsert() {

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "test"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1

        def savedParameterization = parameterization.save()
        assertNotNull savedParameterization
    }

    void testInsertWithParameter() {

        int parameterizationCount = ParameterizationDAO.count()
        int parameterCount = Parameter.count()

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "testInsertWithParameter"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1


        StringParameter parameter = new StringParameter(path: "path", parameterValue: "value")
        parameterization.addToParameters(parameter)

        def savedParameterization = parameterization.save(flush: true)
        assertNotNull savedParameterization
        assertFalse parameterization.hasErrors()

        assertEquals parameterizationCount + 1, ParameterizationDAO.count()
        assertEquals parameterCount + 1, Parameter.count()
        parameterization.discard()

        def reloaded = ParameterizationDAO.findByName("testInsertWithParameter")
        assertNotSame reloaded, parameterization
        def parameters = reloaded.parameters
        assertEquals 1, parameters.size()
    }

    void testAddParameter() {
        int parameterizationCount = ParameterizationDAO.count()
        int parameterCount = Parameter.count()

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "testAddParameter"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "2"
        parameterization.periodCount = 1

        parameterization.save()

        StringParameter parameter = new StringParameter(path: "path", parameterValue: "value")
        parameterization.addToParameters(parameter)

        def savedParameterization = parameterization.save()
        assertNotNull savedParameterization
        assertFalse parameterization.hasErrors()

        assertEquals parameterizationCount + 1, ParameterizationDAO.count()
        assertEquals parameterCount + 1, Parameter.count()

        parameterization.discard()

        def reloaded = ParameterizationDAO.findByName("testAddParameter")
        assertNotSame reloaded, parameterization
        def parameters = reloaded.parameters
        assertEquals 1, parameters.size()

    }

    void testRemoveParameter() {

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "testRemoveParameter"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "2"
        parameterization.periodCount = 1

        parameterization.save()

        StringParameter parameter = new StringParameter(path: "path", parameterValue: "value")
        parameterization.addToParameters(parameter)

        ParameterObjectParameter parameter2 = new ParameterObjectParameter(path: 'path2')
        parameter2.type = new EnumParameter(parameterType: " ", parameterValue: " ", path: "path")
        parameterization.addToParameters(parameter2)


        def savedParameterization = parameterization.save(flush: true)
        assertNotNull savedParameterization
        assertFalse parameterization.hasErrors()


        parameterization.removeFromParameters(parameter)
        parameterization.removeFromParameters(parameter2)
        parameter.delete()
        parameter2.delete()
        parameterization.save(flush: true)

        parameterization.discard()

        def reloaded = ParameterizationDAO.findByName("testRemoveParameter")
        assertNotSame reloaded, parameterization
        def parameters = reloaded.parameters
        assertEquals 0, parameters.size()

    }

    void testDelete() {
        int parameterizationCount = ParameterizationDAO.count()
        int parameterCount = Parameter.count()

        ParameterizationDAO parameterization = new ParameterizationDAO()
        parameterization.name = "test"
        parameterization.modelClassName = EmptyModel.name
        parameterization.itemVersion = "1"
        parameterization.periodCount = 1

        StringParameter parameter = new StringParameter(path: "path", parameterValue: "value")
        parameter.save()
        parameterization.addToParameters(parameter)

        def savedParameterization = parameterization.save()
        assertNotNull savedParameterization
        assertFalse parameterization.hasErrors()

        parameterization.delete()

        assertEquals parameterizationCount, ParameterizationDAO.count()
        assertEquals parameterCount, Parameter.count()

    }

    void testFind() {
        ParameterizationDAO p1 = new ParameterizationDAO(name: 'name1', modelClassName: CoreModel.name, itemVersion: "1", valid: true, periodCount: 1).save()
        assertNotNull p1

        ParameterizationDAO p2 = new ParameterizationDAO(name: 'name1', modelClassName: EmptyModel.name, itemVersion: "1", valid: true, periodCount: 1).save()
        assertNotNull p2

        assertSame p1, ParameterizationDAO.find("name1", CoreModel.name, "1")
        assertSame p2, ParameterizationDAO.find("name1", EmptyModel.name, "1")
        assertNotSame ParameterizationDAO.find("name1", CoreModel.name, "1"), ParameterizationDAO.find("name1", EmptyModel.name, "1")
    }

}