package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.joda.time.DateTime

class ParameterizationImportServiceTests extends GroovyTestCase {


    private ParameterizationImportService getService() {
        return new ParameterizationImportService()
    }


    void testImportParameterization() {

        File paramFile = new File(getModelFolder(), "core/CoreParameters.groovy")

        def count = ParameterizationDAO.count()

        assertTrue "import not successful", service.importFile(paramFile.toURI().toURL())
        assertEquals count + 1, ParameterizationDAO.count()

    }

    void testImportParameterizationWithComments() {

        File paramFile = new File(getModelFolder(), "core/CoreParametersWithComments.groovy")

        def count = ParameterizationDAO.count()

        assertTrue "import not successful", service.importFile(paramFile.toURI().toURL())
        assertEquals count + 1, ParameterizationDAO.count()

        ParameterizationDAO parameterization = ParameterizationDAO.findByName("coreParamTest")
        assertNotNull parameterization
        assertEquals "Error by comments import", parameterization.comments.size(), 1
        List comments = parameterization.comments as List

        assertEquals comments[0].path, "Core:exampleInputOutputComponent:parmParameterObject"
        assertEquals comments[0].periodIndex, 0
        assertEquals comments[0].comment, "comment text"
        assertNull comments[0].user
        assertEquals comments[0].timeStamp, new DateTime(1285144738000)
        List tags = comments[0].tags as List
        assertEquals tags.size(), 1
        assertEquals tags[0].tag.name, "FIXED"

    }

    void testImportParameterizationWithoutComments() {

        File paramFile = new File(getModelFolder(), "core/CoreParametersWithoutComments.groovy")

        def count = ParameterizationDAO.count()

        assertTrue "import not successful", service.importFile(paramFile.toURI().toURL())
        assertEquals count + 1, ParameterizationDAO.count()

        ParameterizationDAO parameterization = ParameterizationDAO.findByName("coreParamWithoutComments")
        assertNotNull parameterization

        assertNull parameterization.comments

    }



    void testPrepare() {

        File paramFile = new File(getModelFolder(), "core/CoreParameters.groovy")

        ParameterizationImportService parameterizationImportService = getService()
        assertEquals "wrong itemName", "CoreParameters", parameterizationImportService.prepare(paramFile.toURI().toURL(), paramFile.name)
        assertNotNull parameterizationImportService.currentConfigObject
        assertEquals "wrong name in config", "CoreParameters", parameterizationImportService.currentConfigObject.displayName

    }

    void testDaoClass() {
        assertEquals "wrong dao class", ParameterizationDAO, service.daoClass
    }

    void testFileSuffix() {
        assertEquals "wrong fileSuffix", "Parameters", service.fileSuffix
    }

    private File getModelFolder() {
        return new File(getClass().getResource("/models").toURI())
    }

}