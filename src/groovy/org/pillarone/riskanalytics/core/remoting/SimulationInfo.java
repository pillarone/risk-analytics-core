package org.pillarone.riskanalytics.core.remoting;

import java.io.Serializable;
import java.util.Date;

public class SimulationInfo implements Serializable {

    private static final long serialVersionUID = 6075668862838699872L;

    private long simulationId;
    private String name;
    private Date runDate;
    private String user;
    private int iterationCount;
    private String resultTemplateName;
    private String comment;
    private int randomSeed;
    private Date updateDate;

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(final int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(final int randomSeed) {
        this.randomSeed = randomSeed;
    }

    public String getResultTemplateName() {
        return resultTemplateName;
    }

    public void setResultTemplateName(final String resultTemplateName) {
        this.resultTemplateName = resultTemplateName;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(final Date runDate) {
        this.runDate = runDate;
    }

    public long getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(final long simulationId) {
        this.simulationId = simulationId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
