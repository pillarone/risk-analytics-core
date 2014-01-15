package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.junit.Test
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.migration.OptionalComponent
import static org.junit.Assert.*

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class ParameterHolderFactoryTests {

    @Test
    void testRename() {
        Parameterization parameterization = new Parameterization("Name")
        parameterization.modelClass = CoreModel
        parameterization.addParameter(new IntegerParameterHolder("myPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced0:subPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath1", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath2", 0, 0))

        int paramCount = parameterization.parameters.size()

        ParameterHolderFactory.renamePathOfParameter(parameterization, "pathToBeReplaced", "newPath", new OptionalComponent())

        List allPaths = parameterization.parameters*.path

        assertEquals paramCount, parameterization.parameters.size()

        assertTrue allPaths.contains("myPath")
        assertTrue allPaths.contains("newPath:subPath1")
        assertTrue allPaths.contains("newPath:subPath2")

    }

    @Test
    void testRename_NewNameSubNameOfOldName() {
        Parameterization parameterization = new Parameterization("Name")
        parameterization.modelClass = CoreModel
        parameterization.addParameter(new IntegerParameterHolder("myPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced0:subPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath1", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath2", 0, 0))

        int paramCount = parameterization.parameters.size()

        ParameterHolderFactory.renamePathOfParameter(parameterization, "pathToBeReplaced", "pathToBeRe", new OptionalComponent())

        List allPaths = parameterization.parameters*.path

        assertEquals paramCount, parameterization.parameters.size()

        assertTrue allPaths.contains("myPath")
        assertTrue allPaths.contains("pathToBeRe:subPath1")
        assertTrue allPaths.contains("pathToBeRe:subPath2")
        assertTrue allPaths.contains("pathToBeReplaced0:subPath")

    }

    @Test
    void testDuplicate() {
        Parameterization parameterization = new Parameterization("Name")
        parameterization.addParameter(new IntegerParameterHolder("myPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced0:subPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath1", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath2", 0, 0))

        int paramCount = parameterization.parameters.size()

        ParameterHolderFactory.duplicateParameters(parameterization, "pathToBeReplaced", "newPath")

        List allPaths = parameterization.parameters*.path

        assertEquals paramCount + 2, parameterization.parameters.size()

        assertTrue allPaths.contains("myPath")
        assertTrue allPaths.contains("pathToBeReplaced:subPath1")
        assertTrue allPaths.contains("pathToBeReplaced:subPath2")
        assertTrue allPaths.contains("newPath:subPath1")
        assertTrue allPaths.contains("newPath:subPath2")

    }
}
