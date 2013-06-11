package org.pillarone.riskanalytics.core.parameterization

import groovy.transform.CompileStatic

/**
 * An ApplicableParameter represents a parameterValue that is defined for a component's param-Property.
 * Calling the apply method sets the parameterValue on the component.
 *
 * ApplicableParameter is used in the ParameterApplicator to store parameterValues per period.
 */
@CompileStatic
public class ApplicableParameter {

    def component
    String parameterPropertyName
    def parameterValue

    public void apply() {
        component[parameterPropertyName] = parameterValue
    }
}