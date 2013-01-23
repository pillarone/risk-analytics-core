package org.pillarone.riskanalytics.core.parameterization.validation;

import org.pillarone.riskanalytics.core.util.GroovyUtils;

import java.util.Collection;
import java.util.Locale;

public abstract class ParameterValidation {

    protected final Collection args;
    protected final String msg;
    private String path;
    private ValidationType validationType;
    private int periodIndex;

    public ParameterValidation(ValidationType validationType, String message, Collection arguments) {
        this.validationType = validationType;
        this.msg = message;
        this.args = arguments;
    }

    public Collection getArgs() {
        return args;
    }

    public String getMsg() {
        return msg;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public void setValidationType(ValidationType validationType) {
        this.validationType = validationType;
    }

    public String getLocalizedMessage(Locale locale) {
        return GroovyUtils.getText(msg, args != null ? args.toArray() : null, locale);
    }

    @Override
    public String toString() {
        return msg;
    }
}
