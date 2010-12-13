package org.pillarone.riskanalytics.core.remoting;

import java.io.Serializable;

public class ParameterizationInfo implements Serializable {

    private static final long serialVersionUID = -4791429469257801819L;

    private long parameterizationId;
    private String name;
    private String user;
    private String comment;
    private String version;

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

    @Override
    public String toString() {
        return "[" + getName() + " v" + getVersion() + " (" + getParameterizationId() + ")]";
    }


}
