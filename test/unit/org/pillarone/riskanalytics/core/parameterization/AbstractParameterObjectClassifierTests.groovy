package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject


class AbstractParameterObjectClassifierTests extends GroovyTestCase {

    void testDateTime() {
        AbstractParameterObjectClassifier classifier = ExampleParameterObjectClassifier.TYPE0
        DateTime dateTime = new DateTime(2009, 1, 2, 3, 4, 5, 6)
        String constructionString = classifier.getConstructionString([date: dateTime])

        assertTrue constructionString.contains("new org.joda.time.DateTime(2009, 1, 2, 3, 4, 5, 6)")

        GroovyShell shell = new GroovyShell(this.getClass().getClassLoader())
        ExampleParameterObject parameterObject = shell.evaluate(constructionString)

        assertEquals 1, parameterObject.parameters.size()
        assertEquals dateTime, parameterObject.parameters.get("date")
    }

    void testString() {
        AbstractParameterObjectClassifier classifier = ExampleParameterObjectClassifier.TYPE0
        String s  = "text"
        String constructionString = classifier.getConstructionString([textProperty: s])

        assertTrue constructionString.contains(/"textProperty":"text"/)

        GroovyShell shell = new GroovyShell(this.getClass().getClassLoader())
        ExampleParameterObject parameterObject = shell.evaluate(constructionString)

        assertEquals 1, parameterObject.parameters.size()
        assertEquals s, parameterObject.parameters.get("textProperty")
    }
}
