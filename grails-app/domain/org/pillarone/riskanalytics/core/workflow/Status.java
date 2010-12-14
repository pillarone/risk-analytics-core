package org.pillarone.riskanalytics.core.workflow;


public enum Status {

    NONE("none"), DATA_ENTRY("data entry"), IN_REVIEW("in review"), REJECTED("rejected"), IN_PRODUCTION("in production");

    private String displayName;

    private Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
