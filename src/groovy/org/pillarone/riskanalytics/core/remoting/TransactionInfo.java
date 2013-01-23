package org.pillarone.riskanalytics.core.remoting;

import java.io.Serializable;

public class TransactionInfo implements Serializable {

    private static final long serialVersionUID = -476461246171310630L;

    private long dealId;
    private String name;

    public TransactionInfo(final long dealId, final String name) {
        this.dealId = dealId;
        this.name = name;
    }

    public long getDealId() {
        return dealId;
    }

    public void setDealId(final long dealId) {
        this.dealId = dealId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
