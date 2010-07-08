package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.simulation.item.Parameterization

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class ParameterHolderFactoryTests extends GroovyTestCase {

    void testRename() {
        Parameterization parameterization = new Parameterization("Name")
        parameterization.addParameter(new IntegerParameterHolder("myPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced0:subPath", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath1", 0, 0))
        parameterization.addParameter(new IntegerParameterHolder("pathToBeReplaced:subPath2", 0, 0))

        int paramCount = parameterization.parameters.size()

        ParameterHolderFactory.renamePathOfParameter(parameterization, "pathToBeReplaced", "newPath")

        List allPaths = parameterization.parameters*.path

        assertEquals paramCount, parameterization.parameters.size()

        assertTrue allPaths.contains("myPath")
        assertTrue allPaths.contains("newPath:subPath1")
        assertTrue allPaths.contains("newPath:subPath2")

    }

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
