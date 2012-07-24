package org.pillarone.riskanalytics.core.simulation.item

import groovy.mock.interceptor.MockFor
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel

class VersionNumberTests extends GroovyTestCase {

    void testParse() {
        assertEquals '1', new VersionNumber('1').toString()
        assertEquals '1.1', new VersionNumber('1.1').toString()
        assertEquals 'R1', new VersionNumber('R1').toString()
        assertEquals 'R1.1', new VersionNumber('R1.1').toString()
        assertEquals '1.1.1', new VersionNumber('1.1.1').toString()
        assertEquals '2.10.111', new VersionNumber('2.10.111').toString()
    }

    void testCompareTo() {
        assertEquals 1, new VersionNumber('2').compareTo(new VersionNumber('1'))
        assertEquals 1, new VersionNumber('1.1').compareTo(new VersionNumber('1'))
        assertEquals new Integer(-1), new VersionNumber('1.1').compareTo(new VersionNumber('2'))
        assertEquals 1, new VersionNumber('1.10').compareTo(new VersionNumber('1.9'))
        assertEquals 0, new VersionNumber('1.10').compareTo(new VersionNumber('1.10'))

        assertEquals 0, new VersionNumber('R1').compareTo(new VersionNumber('R1'))
        assertEquals 1, new VersionNumber('R2').compareTo(new VersionNumber('R1'))
        assertEquals(-1, new VersionNumber('R1').compareTo(new VersionNumber('R2')))
        assertEquals(1, new VersionNumber('R1').compareTo(new VersionNumber('1')))
        assertEquals(1, new VersionNumber('R1').compareTo(new VersionNumber('2')))

    }

    void testChild() {
        assertTrue new VersionNumber('2.1').isDirectChildVersionOf(new VersionNumber('2'))
        assertTrue new VersionNumber('2.1.1').isDirectChildVersionOf(new VersionNumber('2.1'))
        assertFalse new VersionNumber('2.1.1').isDirectChildVersionOf(new VersionNumber('2'))
        assertFalse new VersionNumber('3.1').isDirectChildVersionOf(new VersionNumber('2'))

    }

    void testIncrementVersion() {
        //TODO: MockFor appears to cause issues
        /*ParameterizationDAO dao = new ParameterizationDAO(itemVersion: '1')
        MockFor daoMock = new MockFor(ParameterizationDAO)
        daoMock.demand.findAllByNameAndModelClassName {name, className ->
            [dao]
        }

        Parameterization parameterization = new Parameterization(versionNumber: new VersionNumber('1'), name: '')
        parameterization.modelClass = EmptyModel
        daoMock.use {
            assertEquals '2', VersionNumber.incrementVersion(parameterization).toString()
        }

        dao = new ParameterizationDAO(itemVersion: 'R1')
        daoMock = new MockFor(ParameterizationDAO)
        daoMock.demand.findAllByNameAndModelClassName {name, className ->
            [dao]
        }

        parameterization = new Parameterization(versionNumber: new VersionNumber('R1'), name: '')
        parameterization.modelClass = EmptyModel
        daoMock.use {
            assertEquals 'R2', VersionNumber.incrementVersion(parameterization).toString()
        }

        dao = new ParameterizationDAO(itemVersion: '1')
        ParameterizationDAO dao2 = new ParameterizationDAO(itemVersion: '2')
        daoMock = new MockFor(ParameterizationDAO)
        daoMock.demand.findAllByNameAndModelClassName {name, className ->
            [dao, dao2]
        }

        parameterization = new Parameterization(versionNumber: new VersionNumber('1'), name: '')
        parameterization.modelClass = EmptyModel
        daoMock.use {
            assertEquals '1.1', VersionNumber.incrementVersion(parameterization).toString()
        }

        dao = new ParameterizationDAO(itemVersion: '1')
        dao2 = new ParameterizationDAO(itemVersion: '2')
        ParameterizationDAO dao3 = new ParameterizationDAO(itemVersion: '1.1')
        daoMock = new MockFor(ParameterizationDAO)
        daoMock.demand.findAllByNameAndModelClassName {name, className ->
            [dao, dao2, dao3]
        }

        parameterization = new Parameterization(versionNumber: new VersionNumber('1.1'), name: '')
        parameterization.modelClass = EmptyModel
        daoMock.use {
            assertEquals '1.2', VersionNumber.incrementVersion(parameterization).toString()
        }

        dao = new ParameterizationDAO(itemVersion: '1')
        dao2 = new ParameterizationDAO(itemVersion: '2')
        dao3 = new ParameterizationDAO(itemVersion: '1.1')
        daoMock = new MockFor(ParameterizationDAO)
        daoMock.demand.findAllByNameAndModelClassName {name, className ->
            [dao, dao2, dao3]
        }

        parameterization = new Parameterization(versionNumber: new VersionNumber('1'), name: '')
        parameterization.modelClass = EmptyModel
        daoMock.use {
            assertEquals '1.2', VersionNumber.incrementVersion(parameterization).toString()
        }*/
    }

}