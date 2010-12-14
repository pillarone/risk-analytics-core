package org.pillarone.riskanalytics.core.remoting;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ResultInfo implements Serializable {

    public static class IterationValuePair implements Serializable {

        private static final long serialVersionUID = -4312519169480213890L;

        private int iteration;
        private double value;
        private Date periodDate;

        private IterationValuePair(final int iteration, final double value, final Date date) {
            this.iteration = iteration;
            this.value = value;
            this.periodDate = date;
        }

        public int getIteration() {
            return iteration;
        }

        public double getValue() {
            return value;
        }

        public Date getPeriodDate() {
            return periodDate;
        }
    }

    private static final long serialVersionUID = 7720330345022090658L;

    private String path;
    private int periodIndex;
    private Date periodDate;
    private List<IterationValuePair> values;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Date getPeriodDate() {
        return periodDate;
    }

    @Deprecated
    public void setPeriodDate(final Date periodDate) {
        this.periodDate = periodDate;
    }

    @Deprecated
    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(final int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public List<IterationValuePair> getValues() {
        return values;
    }

    public void setValues(final List<IterationValuePair> values) {
        this.values = values;
    }
}
