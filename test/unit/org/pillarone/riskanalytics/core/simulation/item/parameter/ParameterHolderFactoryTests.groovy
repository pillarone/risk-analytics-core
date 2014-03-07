package org.pillarone.riskanalytics.core.simulation.item.parameter

import models.core.CoreModel
import org.junit.Test
import org.pillarone.riskanalytics.core.example.migration.OptionalComponent
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

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
                                                                1
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

    @Test
    void testDuplicateWithClassifiers() {
        List<ParameterHolder> parameters = new ArrayList()
        parameters << ParameterHolderFactory.getHolder("replace:parmNomatter", 0, ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0, "b": 0]))
        parameters << ParameterHolderFactory.getHolder("replace:parmHidiho", 2, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0, "p2": 0]))
        parameters << ParameterHolderFactory.getHolder("notreplace:parmHidiho", 1, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0, "p2": 0]))
        Parameterization parametrization = new Parameterization("")
        parameters.each { parametrization.addParameter(it) }

        int paramCount = parametrization.parameters.size()
        assert 3 == paramCount

        ParameterHolderFactory.duplicateParameters(parametrization, "replace", "newPath")

        assert 5 == parametrization.parameters.size()

        List allPaths = parametrization.parameters*.path
        assertTrue allPaths.contains("newPath:parmNomatter")
        assertTrue allPaths.contains("newPath:parmHidiho")
        assertTrue allPaths.contains("replace:parmNomatter")
        assertTrue allPaths.contains("replace:parmHidiho")
        assertTrue allPaths.contains("notreplace:parmHidiho")

        ParameterObjectParameterHolder newPathNoMatter = parametrization.getParameterHolder('newPath:parmNomatter', 0) as ParameterObjectParameterHolder
        assert newPathNoMatter.classifierParameters.size() == 2
        assert newPathNoMatter.classifierParameters.keySet().contains('a')
        assert newPathNoMatter.classifierParameters.keySet().contains('b')
        assert newPathNoMatter.classifierParameters.values().path.contains('newPath:parmNomatter:a')
        assert newPathNoMatter.classifierParameters.values().path.contains('newPath:parmNomatter:b')

        ParameterObjectParameterHolder newPathHidiHo = parametrization.getParameterHolder('newPath:parmHidiho', 2) as ParameterObjectParameterHolder
        assert newPathHidiHo.classifierParameters.size() == 2
        assert newPathHidiHo.classifierParameters.keySet().contains('p1')
        assert newPathHidiHo.classifierParameters.keySet().contains('p2')
        assert newPathHidiHo.classifierParameters.values().path.contains('newPath:parmHidiho:p1')
        assert newPathHidiHo.classifierParameters.values().path.contains('newPath:parmHidiho:p2')

    }
}
