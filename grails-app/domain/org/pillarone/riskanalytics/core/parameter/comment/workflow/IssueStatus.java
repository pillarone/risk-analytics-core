package org.pillarone.riskanalytics.core.parameter.comment.workflow;

public enum IssueStatus {

    OPEN("Open"), RESOLVED("Resolved"), CLOSED("Closed");

    private String displayName;

    private IssueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
