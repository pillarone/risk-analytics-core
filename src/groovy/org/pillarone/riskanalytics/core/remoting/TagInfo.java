package org.pillarone.riskanalytics.core.remoting;

import java.io.Serializable;

/**
 * Allianz Risk Transfer  ATOM
 * User: bzetterstrom
 */
public class TagInfo implements Serializable {
    private static final long serialVersionUID = -2931265100296967543L;

    private long tagId;
    private String name;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(final long tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[" + getName() + " (" + getTagId() + ")]";
    }
}