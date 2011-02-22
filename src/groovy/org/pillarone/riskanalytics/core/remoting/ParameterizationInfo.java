package org.pillarone.riskanalytics.core.remoting;

import org.pillarone.riskanalytics.core.workflow.Status;

import java.io.Serializable;
import java.util.Date;

public class ParameterizationInfo implements Serializable {

    private static final long serialVersionUID = -4791429469257801820L;

    private long parameterizationId;
    private String name;
    private String user;
    private String comment;
    private String version;

    private Status status;
    private Date valuationDate;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getParameterizationId() {
        return parameterizationId;
    }

    public void setParameterizationId(final long parameterizationId) {
        this.parameterizationId = parameterizationId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(Date valuationDate) {
        this.valuationDate = valuationDate;
    }

    @Override
    public String toString() {
        return "[" + getName() + " v" + getVersion() + " (" + getParameterizationId() + ")]";
    }


}
